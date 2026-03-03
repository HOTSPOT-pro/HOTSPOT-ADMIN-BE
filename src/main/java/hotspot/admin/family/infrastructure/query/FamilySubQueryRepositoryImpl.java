package hotspot.admin.family.infrastructure.query;

import java.sql.Array;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import hotspot.admin.family.domain.FamilyRole;
import hotspot.admin.family.infrastructure.query.dto.FamilyControlMemberRow;
import hotspot.admin.family.infrastructure.query.dto.FamilyPolicyAppPolicyRow;
import hotspot.admin.family.infrastructure.query.dto.FamilyPolicyMemberRow;
import hotspot.admin.family.infrastructure.query.dto.FamilyPolicyStatusRow;
import hotspot.admin.family.infrastructure.query.dto.FamilyPolicyTimePolicyRow;
import hotspot.admin.family.service.port.FamilySubQueryRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class FamilySubQueryRepositoryImpl implements FamilySubQueryRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    /** 제어 탭 구성원 목록(역할/차단/한도/우선순위)을 조회한다. */
    @Override
    public List<FamilyControlMemberRow> findFamilyControlMembers(Long familyId) {
        String sql = """
                SELECT
                    s.sub_id,
                    m.name AS member_name,
                    fs.family_role,
                    s.is_locked AS blocked,
                    fs.data_limit,
                    fs.priority
                FROM family_sub fs
                JOIN family f ON f.family_id = fs.family_id AND f.is_deleted = false
                JOIN subscription s ON s.sub_id = fs.sub_id AND s.is_deleted = false
                JOIN member m ON m.member_id = s.member_id AND m.is_deleted = false
                WHERE fs.family_id = :familyId
                ORDER BY
                    CASE fs.family_role
                        WHEN :ownerRole THEN 1
                        WHEN :parentRole THEN 2
                        WHEN :childRole THEN 3
                        ELSE 4
                    END,
                    fs.priority ASC NULLS LAST,
                    m.name ASC
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("familyId", familyId)
                .addValue("ownerRole", FamilyRole.OWNER.name())
                .addValue("parentRole", FamilyRole.PARENT.name())
                .addValue("childRole", FamilyRole.CHILD.name());

        return jdbcTemplate.query(sql, params, this::mapFamilyControlMemberRow);
    }

    /** 정책 탭 구성원 기본 정보(이름/전화/역할/차단)를 조회한다. */
    @Override
    public List<FamilyPolicyMemberRow> findFamilyPolicyMembers(Long familyId) {
        String sql = """
                SELECT
                    s.sub_id,
                    m.name AS member_name,
                    s.phone_enc AS phone_number_enc,
                    fs.family_role,
                    s.is_locked AS blocked
                FROM family_sub fs
                JOIN family f ON f.family_id = fs.family_id AND f.is_deleted = false
                JOIN subscription s ON s.sub_id = fs.sub_id AND s.is_deleted = false
                JOIN member m ON m.member_id = s.member_id AND m.is_deleted = false
                WHERE fs.family_id = :familyId
                ORDER BY
                    CASE fs.family_role
                        WHEN :ownerRole THEN 1
                        WHEN :parentRole THEN 2
                        WHEN :childRole THEN 3
                        ELSE 4
                    END,
                    m.name ASC
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("familyId", familyId)
                .addValue("ownerRole", FamilyRole.OWNER.name())
                .addValue("parentRole", FamilyRole.PARENT.name())
                .addValue("childRole", FamilyRole.CHILD.name());

        return jdbcTemplate.query(sql, params, this::mapFamilyPolicyMemberRow);
    }

    /** 구성원별 적용 시간대 정책명을 조회한다. */
    @Override
    public List<FamilyPolicyTimePolicyRow> findFamilyTimePolicies(Long familyId) {
        String sql = """
                SELECT DISTINCT
                    ps.sub_id,
                    bp.policy_name
                FROM family_sub fs
                JOIN family f ON f.family_id = fs.family_id AND f.is_deleted = false
                JOIN subscription s ON s.sub_id = fs.sub_id AND s.is_deleted = false
                JOIN policy_sub ps ON ps.sub_id = s.sub_id AND ps.is_active = true
                JOIN block_policy bp
                  ON bp.block_policy_id = ps.block_policy_id
                 AND bp.is_active = true
                 AND bp.is_deleted = false
                WHERE fs.family_id = :familyId
                ORDER BY ps.sub_id, policy_name
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("familyId", familyId);

        return jdbcTemplate.query(sql, params, this::mapFamilyTimePolicyRow);
    }

    /** 구성원별 적용 차단 서비스 정책명을 조회한다. */
    @Override
    public List<FamilyPolicyAppPolicyRow> findFamilyAppPolicies(Long familyId) {
        String sql = """
                SELECT DISTINCT
                    bss.sub_id,
                    abs.blocked_service_name
                FROM family_sub fs
                JOIN family f ON f.family_id = fs.family_id AND f.is_deleted = false
                JOIN subscription s ON s.sub_id = fs.sub_id AND s.is_deleted = false
                JOIN blocked_service_sub bss ON bss.sub_id = s.sub_id AND bss.is_deleted = false
                JOIN app_blocked_service abs
                  ON abs.app_blocked_service_id = bss.blocked_service_id
                 AND abs.is_deleted = false
                WHERE fs.family_id = :familyId
                ORDER BY bss.sub_id, abs.blocked_service_name
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("familyId", familyId);

        return jdbcTemplate.query(sql, params, this::mapFamilyAppPolicyRow);
    }

    /** 정책 탭 화면용 통합 행을 한 번의 조회로 구성한다. */
    @Override
    public List<FamilyPolicyStatusRow> findFamilyPolicyStatusRows(Long familyId) {
        String sql = """
                SELECT
                    s.sub_id,
                    m.name AS member_name,
                    s.phone_enc AS phone_number_enc,
                    fs.family_role,
                    s.is_locked AS blocked,
                    COALESCE(tp.time_policy_names, ARRAY[]::text[]) AS time_policy_names,
                    COALESCE(ap.app_policy_names, ARRAY[]::text[]) AS app_policy_names
                FROM family_sub fs
                JOIN family f ON f.family_id = fs.family_id AND f.is_deleted = false
                JOIN subscription s ON s.sub_id = fs.sub_id AND s.is_deleted = false
                JOIN member m ON m.member_id = s.member_id AND m.is_deleted = false
                LEFT JOIN LATERAL (
                    SELECT array_agg(DISTINCT bp.policy_name
                            ORDER BY bp.policy_name) AS time_policy_names
                    FROM policy_sub ps
                    JOIN block_policy bp
                      ON bp.block_policy_id = ps.block_policy_id
                     AND bp.is_active = true
                     AND bp.is_deleted = false
                    WHERE ps.sub_id = s.sub_id
                      AND ps.is_active = true
                      AND COALESCE(bp.policy_name, '') <> ''
                ) tp ON true
                LEFT JOIN LATERAL (
                    SELECT array_agg(DISTINCT abs.blocked_service_name
                            ORDER BY abs.blocked_service_name) AS app_policy_names
                    FROM blocked_service_sub bss
                    JOIN app_blocked_service abs
                      ON abs.app_blocked_service_id = bss.blocked_service_id
                     AND abs.is_deleted = false
                    WHERE bss.sub_id = s.sub_id
                      AND bss.is_deleted = false
                ) ap ON true
                WHERE fs.family_id = :familyId
                ORDER BY
                    CASE fs.family_role
                        WHEN :ownerRole THEN 1
                        WHEN :parentRole THEN 2
                        WHEN :childRole THEN 3
                        ELSE 4
                    END,
                    m.name ASC
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("familyId", familyId)
                .addValue("ownerRole", FamilyRole.OWNER.name())
                .addValue("parentRole", FamilyRole.PARENT.name())
                .addValue("childRole", FamilyRole.CHILD.name());

        return jdbcTemplate.query(sql, params, this::mapFamilyPolicyStatusRow);
    }

    /** 정책 탭 구성원 기본 행을 DTO로 변환한다. */
    private FamilyPolicyMemberRow mapFamilyPolicyMemberRow(java.sql.ResultSet rs, int rowNum)
            throws java.sql.SQLException {
        return FamilyPolicyMemberRow.builder()
                .subId(rs.getLong("sub_id"))
                .memberName(rs.getString("member_name"))
                .phoneNumberEnc(rs.getString("phone_number_enc"))
                .familyRole(FamilyRole.valueOf(rs.getString("family_role")))
                .blocked(rs.getBoolean("blocked"))
                .build();
    }

    /** 제어 탭 구성원 행을 DTO로 변환한다. */
    private FamilyControlMemberRow mapFamilyControlMemberRow(java.sql.ResultSet rs, int rowNum)
            throws java.sql.SQLException {
        return FamilyControlMemberRow.builder()
                .subId(rs.getLong("sub_id"))
                .memberName(rs.getString("member_name"))
                .familyRole(FamilyRole.valueOf(rs.getString("family_role")))
                .blocked(rs.getBoolean("blocked"))
                .dataLimit(rs.getObject("data_limit", Long.class))
                .priority(rs.getObject("priority", Integer.class))
                .build();
    }

    /** 시간대 정책 행을 DTO로 변환한다. */
    private FamilyPolicyTimePolicyRow mapFamilyTimePolicyRow(java.sql.ResultSet rs, int rowNum)
            throws java.sql.SQLException {
        return FamilyPolicyTimePolicyRow.builder()
                .subId(rs.getLong("sub_id"))
                .policyName(rs.getString("policy_name"))
                .build();
    }

    /** 차단 서비스 정책 행을 DTO로 변환한다. */
    private FamilyPolicyAppPolicyRow mapFamilyAppPolicyRow(java.sql.ResultSet rs, int rowNum)
            throws java.sql.SQLException {
        return FamilyPolicyAppPolicyRow.builder()
                .subId(rs.getLong("sub_id"))
                .blockedServiceName(rs.getString("blocked_service_name"))
                .build();
    }

    /** 정책 탭 통합 행을 DTO로 변환한다. */
    private FamilyPolicyStatusRow mapFamilyPolicyStatusRow(java.sql.ResultSet rs, int rowNum) throws SQLException {
        return FamilyPolicyStatusRow.builder()
                .subId(rs.getLong("sub_id"))
                .memberName(rs.getString("member_name"))
                .phoneNumberEnc(rs.getString("phone_number_enc"))
                .familyRole(FamilyRole.valueOf(rs.getString("family_role")))
                .blocked(rs.getBoolean("blocked"))
                .appliedTimePolicies(toStringList(rs.getArray("time_policy_names")))
                .appliedBlockedServicePolicies(toStringList(rs.getArray("app_policy_names")))
                .build();
    }

    /** PostgreSQL text[] 배열을 Java List<String>으로 변환한다. */
    private List<String> toStringList(Array sqlArray) throws SQLException {
        if (sqlArray == null) {
            return Collections.emptyList();
        }
        Object value = sqlArray.getArray();
        if (!(value instanceof String[] arr)) {
            return Collections.emptyList();
        }
        return Arrays.asList(arr);
    }
}
