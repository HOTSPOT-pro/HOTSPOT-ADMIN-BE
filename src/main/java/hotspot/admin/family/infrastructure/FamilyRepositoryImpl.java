package hotspot.admin.family.infrastructure;

import java.sql.Array;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import hotspot.admin.family.controller.response.FamilyListItem;
import hotspot.admin.family.controller.response.FamilyRequestListItem;
import hotspot.admin.family.domain.ApplyType;
import hotspot.admin.family.domain.FamilyApplyStatus;
import hotspot.admin.family.domain.FamilyRole;
import hotspot.admin.family.domain.PriorityType;
import hotspot.admin.family.service.dto.FamilyControlMemberRow;
import hotspot.admin.family.service.dto.FamilyPolicyAppPolicyRow;
import hotspot.admin.family.service.dto.FamilyPolicyMemberRow;
import hotspot.admin.family.service.dto.FamilyPolicyStatusRow;
import hotspot.admin.family.service.dto.FamilyPolicyTimePolicyRow;
import hotspot.admin.family.service.port.FamilyRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class FamilyRepositoryImpl implements FamilyRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public List<FamilyListItem> findFamilyList(int limit, long offset) {
        String sql = """
                WITH family_agg AS (
                    SELECT
                        f.family_id,
                        COALESCE(
                            MAX(CASE WHEN fs.family_role = :ownerRole THEN m.name END),
                            MAX(CASE WHEN fs.family_role = :parentRole THEN m.name END),
                            MIN(m.name)
                        ) AS representative_name,
                        COALESCE(
                            MAX(CASE WHEN fs.family_role = :ownerRole THEN s.phone_enc END),
                            MAX(CASE WHEN fs.family_role = :parentRole THEN s.phone_enc END),
                            MIN(s.phone_enc)
                        ) AS phone_number_enc,
                        COUNT(fs.family_sub_id)::int AS member_count
                    FROM family f
                    LEFT JOIN family_sub fs ON fs.family_id = f.family_id
                    LEFT JOIN subscription s ON s.sub_id = fs.sub_id AND s.is_deleted = false
                    LEFT JOIN member m ON m.member_id = s.member_id AND m.is_deleted = false
                    WHERE f.is_deleted = false
                    GROUP BY f.family_id
                )
                SELECT family_id, representative_name, phone_number_enc, member_count
                FROM family_agg
                ORDER BY family_id ASC
                LIMIT :limit
                OFFSET :offset
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("limit", limit)
                .addValue("offset", offset)
                .addValue("ownerRole", FamilyRole.OWNER.name())
                .addValue("parentRole", FamilyRole.PARENT.name());

        return jdbcTemplate.query(sql, params, this::mapFamilyListItem);
    }

    @Override
    public long countFamilyList() {
        String sql = """
                SELECT COUNT(*)::bigint
                FROM family f
                WHERE f.is_deleted = false
                """;

        Long total = jdbcTemplate.queryForObject(sql, new MapSqlParameterSource(), Long.class);
        return total == null ? 0L : total;
    }

    @Override
    public Optional<FamilyListItem> findFamilyByPhoneHash(String phoneHash) {
        String sql = """
                WITH family_agg AS (
                    SELECT
                        f.family_id,
                        COALESCE(
                            MAX(CASE WHEN fs.family_role = :ownerRole THEN m.name END),
                            MAX(CASE WHEN fs.family_role = :parentRole THEN m.name END),
                            MIN(m.name)
                        ) AS representative_name,
                        COALESCE(
                            MAX(CASE WHEN fs.family_role = :ownerRole THEN s.phone_enc END),
                            MAX(CASE WHEN fs.family_role = :parentRole THEN s.phone_enc END),
                            MIN(s.phone_enc)
                        ) AS phone_number_enc,
                        COUNT(fs.family_sub_id)::int AS member_count
                    FROM family f
                    LEFT JOIN family_sub fs ON fs.family_id = f.family_id
                    LEFT JOIN subscription s ON s.sub_id = fs.sub_id AND s.is_deleted = false
                    LEFT JOIN member m ON m.member_id = s.member_id AND m.is_deleted = false
                    WHERE f.is_deleted = false
                    AND EXISTS (
                        SELECT 1
                        FROM family_sub fs2
                        JOIN subscription s2 ON s2.sub_id = fs2.sub_id
                        WHERE fs2.family_id = f.family_id
                        AND s2.is_deleted = false
                        AND s2.phone_hash = :phoneHash
                    )
                    GROUP BY f.family_id
                )
                SELECT family_id, representative_name, phone_number_enc, member_count
                FROM family_agg
                ORDER BY family_id ASC
                LIMIT 1
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("phoneHash", phoneHash)
                .addValue("ownerRole", FamilyRole.OWNER.name())
                .addValue("parentRole", FamilyRole.PARENT.name());

        List<FamilyListItem> result = jdbcTemplate.query(sql, params, this::mapFamilyListItem);
        return result.stream().findFirst();
    }

    @Override
    public Optional<FamilyListItem> findFamilyById(Long familyId) {
        String sql = """
                WITH family_agg AS (
                    SELECT
                        f.family_id,
                        COALESCE(
                            MAX(CASE WHEN fs.family_role = :ownerRole THEN m.name END),
                            MAX(CASE WHEN fs.family_role = :parentRole THEN m.name END),
                            MIN(m.name)
                        ) AS representative_name,
                        COALESCE(
                            MAX(CASE WHEN fs.family_role = :ownerRole THEN s.phone_enc END),
                            MAX(CASE WHEN fs.family_role = :parentRole THEN s.phone_enc END),
                            MIN(s.phone_enc)
                        ) AS phone_number_enc,
                        COUNT(fs.family_sub_id)::int AS member_count
                    FROM family f
                    LEFT JOIN family_sub fs ON fs.family_id = f.family_id
                    LEFT JOIN subscription s ON s.sub_id = fs.sub_id AND s.is_deleted = false
                    LEFT JOIN member m ON m.member_id = s.member_id AND m.is_deleted = false
                    WHERE f.is_deleted = false
                      AND f.family_id = :familyId
                    GROUP BY f.family_id
                )
                SELECT family_id, representative_name, phone_number_enc, member_count
                FROM family_agg
                LIMIT 1
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("familyId", familyId)
                .addValue("ownerRole", FamilyRole.OWNER.name())
                .addValue("parentRole", FamilyRole.PARENT.name());

        List<FamilyListItem> result = jdbcTemplate.query(sql, params, this::mapFamilyListItem);
        return result.stream().findFirst();
    }

    /** 가족 상세 제어 기능 탭에서 사용할 우선순위 유형(FIFO/PRIORITY) 단건 조회 */
    @Override
    public Optional<PriorityType> findFamilyPriorityType(Long familyId) {
        String sql = """
                SELECT f.priority_type
                FROM family f
                WHERE f.family_id = :familyId
                  AND f.is_deleted = false
                LIMIT 1
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("familyId", familyId);

        List<PriorityType> result = jdbcTemplate.query(sql, params,
                (rs, rowNum) -> PriorityType.valueOf(rs.getString("priority_type")));
        return result.stream().findFirst();
    }

    /** 가족 상세 제어 기능 탭에서 사용할 구성원별 제어 정보 조회 */
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

    /** 가족 상세 정책 탭의 구성원 기본 정보(회선/역할/차단 상태) 조회 */
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

    /** 가족 상세 정책 탭의 구성원별 시간대 정책명 조회 */
    @Override
    public List<FamilyPolicyTimePolicyRow> findFamilyTimePolicies(Long familyId) {
        String sql = """
                SELECT DISTINCT
                    ps.sub_id,
                    ps.date_snapshot->>'policyName' AS policy_name
                FROM family_sub fs
                JOIN family f ON f.family_id = fs.family_id AND f.is_deleted = false
                JOIN subscription s ON s.sub_id = fs.sub_id AND s.is_deleted = false
                JOIN policy_sub ps ON ps.sub_id = s.sub_id AND ps.is_deleted = false
                WHERE fs.family_id = :familyId
                ORDER BY ps.sub_id, policy_name
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("familyId", familyId);

        return jdbcTemplate.query(sql, params, this::mapFamilyTimePolicyRow);
    }

    /** 가족 상세 정책 탭의 구성원별 차단 서비스 정책명 조회 */
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
                    SELECT array_agg(DISTINCT ps.date_snapshot->>'policyName'
                            ORDER BY ps.date_snapshot->>'policyName') AS time_policy_names
                    FROM policy_sub ps
                    WHERE ps.sub_id = s.sub_id
                      AND ps.is_deleted = false
                      AND COALESCE(ps.date_snapshot->>'policyName', '') <> ''
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

    @Override
    public List<FamilyRequestListItem> findFamilyRequestList(
            ApplyType applyType,
            FamilyApplyStatus status,
            int limit,
            long offset
    ) {
        String sql = """
                SELECT
                    fa.family_apply_id,
                    fa.family_id,
                    fa.apply_type,
                    fa.status,
                    fa.target_family_role,
                    rm.name AS requester_name,
                    rs.phone_enc AS requester_phone_enc,
                    tm.name AS target_name,
                    ts.phone_enc AS target_phone_enc,
                    fa.doc_url,
                    fa.created_time
                FROM family_apply fa
                JOIN subscription rs ON rs.sub_id = fa.requester_sub_id AND rs.is_deleted = false
                JOIN member rm ON rm.member_id = rs.member_id AND rm.is_deleted = false
                JOIN subscription ts ON ts.sub_id = fa.target_sub_id AND ts.is_deleted = false
                JOIN member tm ON tm.member_id = ts.member_id AND tm.is_deleted = false
                JOIN family f ON f.family_id = fa.family_id AND f.is_deleted = false
                WHERE fa.apply_type = :applyType
                  AND fa.status = :status
                ORDER BY fa.created_time DESC
                LIMIT :limit
                OFFSET :offset
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("applyType", applyType.name())
                .addValue("status", status.name())
                .addValue("limit", limit)
                .addValue("offset", offset);

        return jdbcTemplate.query(sql, params, this::mapFamilyRequestItem);
    }

    @Override
    public long countFamilyRequestList(ApplyType applyType, FamilyApplyStatus status) {
        String sql = """
                SELECT COUNT(*)::bigint
                FROM family_apply fa
                JOIN family f ON f.family_id = fa.family_id AND f.is_deleted = false
                WHERE fa.apply_type = :applyType
                  AND fa.status = :status
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("applyType", applyType.name())
                .addValue("status", status.name());

        Long total = jdbcTemplate.queryForObject(sql, params, Long.class);
        return total == null ? 0L : total;
    }

    @Override
    public int updateFamilyRequestStatus(
            Long familyApplyId,
            ApplyType applyType,
            FamilyApplyStatus currentStatus,
            FamilyApplyStatus newStatus
    ) {
        String sql = """
                UPDATE family_apply
                SET status = :newStatus,
                    modified_time = NOW()
                WHERE family_apply_id = :familyApplyId
                  AND apply_type = :applyType
                  AND status = :currentStatus
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("familyApplyId", familyApplyId)
                .addValue("applyType", applyType.name())
                .addValue("currentStatus", currentStatus.name())
                .addValue("newStatus", newStatus.name());

        return jdbcTemplate.update(sql, params);
    }

    @Override
    public boolean existsFamilyRequest(Long familyApplyId, ApplyType applyType) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM family_apply
                    WHERE family_apply_id = :familyApplyId
                      AND apply_type = :applyType
                )
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("familyApplyId", familyApplyId)
                .addValue("applyType", applyType.name());

        Boolean exists = jdbcTemplate.queryForObject(sql, params, Boolean.class);
        return Boolean.TRUE.equals(exists);
    }

    private FamilyListItem mapFamilyListItem(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return FamilyListItem.builder()
                .familyId(rs.getLong("family_id"))
                .representativeName(rs.getString("representative_name"))
                .phoneNumber(rs.getString("phone_number_enc"))
                .memberCount(rs.getInt("member_count"))
                .build();
    }

    private FamilyRequestListItem mapFamilyRequestItem(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return FamilyRequestListItem.builder()
                .requestId(rs.getLong("family_apply_id"))
                .familyId(rs.getLong("family_id"))
                .requesterName(rs.getString("requester_name"))
                .requesterPhoneNumber(rs.getString("requester_phone_enc"))
                .targetName(rs.getString("target_name"))
                .targetPhoneNumber(rs.getString("target_phone_enc"))
                .targetFamilyRole(FamilyRole.valueOf(rs.getString("target_family_role")))
                .relationDocumentUrl(rs.getString("doc_url"))
                .requestedAt(rs.getTimestamp("created_time").toLocalDateTime())
                .build();
    }

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

    private FamilyPolicyTimePolicyRow mapFamilyTimePolicyRow(java.sql.ResultSet rs, int rowNum)
            throws java.sql.SQLException {
        return FamilyPolicyTimePolicyRow.builder()
                .subId(rs.getLong("sub_id"))
                .policyName(rs.getString("policy_name"))
                .build();
    }

    private FamilyPolicyAppPolicyRow mapFamilyAppPolicyRow(java.sql.ResultSet rs, int rowNum)
            throws java.sql.SQLException {
        return FamilyPolicyAppPolicyRow.builder()
                .subId(rs.getLong("sub_id"))
                .blockedServiceName(rs.getString("blocked_service_name"))
                .build();
    }

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
