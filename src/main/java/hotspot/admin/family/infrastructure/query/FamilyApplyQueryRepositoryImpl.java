package hotspot.admin.family.infrastructure.query;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import hotspot.admin.family.controller.response.FamilyRequestListItem;
import hotspot.admin.family.domain.ApplyType;
import hotspot.admin.family.domain.FamilyApplyStatus;
import hotspot.admin.family.domain.FamilyRole;
import hotspot.admin.family.service.port.FamilyApplyQueryRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class FamilyApplyQueryRepositoryImpl implements FamilyApplyQueryRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    /** 가족 요청 목록(결합/해제)을 상태 기준으로 페이지 조회한다. */
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

    /** 가족 요청 목록 총 건수를 조회한다. */
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

    /** 요청 목록 조회 결과 행을 응답 DTO로 변환한다. */
    private FamilyRequestListItem mapFamilyRequestItem(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        LocalDateTime requestedAt = rs.getTimestamp("created_time").toLocalDateTime();
        return FamilyRequestListItem.builder()
                .requestId(rs.getLong("family_apply_id"))
                .familyId(rs.getLong("family_id"))
                .requesterName(rs.getString("requester_name"))
                .requesterPhoneNumber(rs.getString("requester_phone_enc"))
                .targetName(rs.getString("target_name"))
                .targetPhoneNumber(rs.getString("target_phone_enc"))
                .targetFamilyRole(FamilyRole.valueOf(rs.getString("target_family_role")))
                .relationDocumentUrl(rs.getString("doc_url"))
                .requestedAt(requestedAt)
                .build();
    }
}
