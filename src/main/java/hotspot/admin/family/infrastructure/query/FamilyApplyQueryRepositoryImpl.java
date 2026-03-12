package hotspot.admin.family.infrastructure.query;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import hotspot.admin.family.domain.ApplyType;
import hotspot.admin.family.domain.FamilyApplyStatus;
import hotspot.admin.family.domain.FamilyRole;
import hotspot.admin.family.infrastructure.query.dto.FamilyApprovalTargetInfo;
import hotspot.admin.family.infrastructure.query.dto.FamilyRequestListRow;
import hotspot.admin.family.service.port.FamilyApplyQueryRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class FamilyApplyQueryRepositoryImpl implements FamilyApplyQueryRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    /** 가족 요청 목록(결합/해제)을 상태 기준으로 페이지 조회한다. */
    @Override
    public List<FamilyRequestListRow> findFamilyRequestList(
            ApplyType applyType,
            FamilyApplyStatus status,
            int limit,
            long offset
    ) {
        String sql = """
                WITH paged_request AS (
                    SELECT
                        fa.family_apply_id,
                        fa.family_id,
                        fa.requester_sub_id,
                        fa.doc_url,
                        fa.created_time
                    FROM family_apply fa
                    WHERE fa.apply_type = :applyType
                      AND fa.status = :status
                    ORDER BY fa.family_apply_id DESC
                    LIMIT :limit
                    OFFSET :offset
                )
                SELECT
                    pr.family_apply_id,
                    pr.family_id,
                    pr.requester_sub_id,
                    fat.target_sub_id,
                    fat.target_family_role,
                    rm.name AS requester_name,
                    rs.phone_enc AS requester_phone_enc,
                    tm.name AS target_name,
                    ts.phone_enc AS target_phone_enc,
                    pr.doc_url,
                    pr.created_time
                FROM paged_request pr
                JOIN subscription rs ON rs.sub_id = pr.requester_sub_id AND rs.is_deleted = false
                JOIN member rm ON rm.member_id = rs.member_id AND rm.is_deleted = false
                LEFT JOIN family_apply_target fat ON fat.family_apply_id = pr.family_apply_id
                LEFT JOIN subscription ts ON ts.sub_id = fat.target_sub_id AND ts.is_deleted = false
                LEFT JOIN member tm ON tm.member_id = ts.member_id AND tm.is_deleted = false
                ORDER BY pr.family_apply_id DESC, fat.family_apply_target_id ASC NULLS LAST
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("applyType", applyType.name())
                .addValue("status", status.name())
                .addValue("limit", limit)
                .addValue("offset", offset);

        return jdbcTemplate.query(sql, params, this::mapFamilyRequestItem);
    }

    /** 가족 요청 목록 총 건수를 조회한다. */
    @Override
    public long countFamilyRequestList(ApplyType applyType, FamilyApplyStatus status) {
        String sql = """
                SELECT COUNT(*)::bigint
                FROM family_apply fa
                WHERE fa.apply_type = :applyType
                  AND fa.status = :status
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("applyType", applyType.name())
                .addValue("status", status.name());

        Long total = jdbcTemplate.queryForObject(sql, params, Long.class);
        return total == null ? 0L : total;
    }

    /** 승인 후처리에 필요한 대상자 목록을 조회한다. */
    @Override
    public List<FamilyApprovalTargetInfo> findApprovalTargetInfos(Long familyApplyId, ApplyType applyType) {
        String sql = """
                SELECT
                    fa.family_id,
                    fat.target_sub_id,
                    fat.target_family_role
                FROM family_apply fa
                JOIN family_apply_target fat ON fat.family_apply_id = fa.family_apply_id
                WHERE fa.family_apply_id = :familyApplyId
                  AND fa.apply_type = :applyType
                ORDER BY fat.family_apply_target_id
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("familyApplyId", familyApplyId)
                .addValue("applyType", applyType.name());

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> FamilyApprovalTargetInfo.builder()
                .familyId(rs.getObject("family_id", Long.class))
                .targetSubId(rs.getLong("target_sub_id"))
                .targetFamilyRole(FamilyRole.valueOf(rs.getString("target_family_role")))
                .build());
    }

    /** 요청 목록 조회 결과 행을 응답 DTO로 변환한다. */
    private FamilyRequestListRow mapFamilyRequestItem(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        LocalDateTime requestedAt = rs.getTimestamp("created_time").toLocalDateTime();
        String targetFamilyRole = rs.getString("target_family_role");
        return FamilyRequestListRow.builder()
                .requestId(rs.getLong("family_apply_id"))
                .familyId(rs.getObject("family_id", Long.class))
                .requestSubId(rs.getObject("requester_sub_id", Long.class))
                .requesterName(rs.getString("requester_name"))
                .requesterPhoneNumberEnc(rs.getString("requester_phone_enc"))
                .targetSubId(rs.getObject("target_sub_id", Long.class))
                .targetName(rs.getString("target_name"))
                .targetPhoneNumberEnc(rs.getString("target_phone_enc"))
                .targetFamilyRole(targetFamilyRole == null ? null : FamilyRole.valueOf(targetFamilyRole))
                .relationDocumentUrl(rs.getString("doc_url"))
                .requestedAt(requestedAt)
                .build();
    }
}
