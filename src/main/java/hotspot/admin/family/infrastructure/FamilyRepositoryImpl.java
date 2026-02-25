package hotspot.admin.family.infrastructure;

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
}
