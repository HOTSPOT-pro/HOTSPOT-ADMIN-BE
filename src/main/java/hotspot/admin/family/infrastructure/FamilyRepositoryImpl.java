package hotspot.admin.family.infrastructure;

import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import hotspot.admin.family.controller.response.FamilyListItem;
import hotspot.admin.family.domain.FamilyRole;
import hotspot.admin.family.service.port.FamilyRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class FamilyRepositoryImpl implements FamilyRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public List<FamilyListItem> findFamilySlice(int limitPlusOne, Long cursorFamilyId) {
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
                WHERE family_id > :cursorFamilyId
                ORDER BY family_id ASC
                LIMIT :limit
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("cursorFamilyId", cursorFamilyId)
                .addValue("limit", limitPlusOne)
                .addValue("ownerRole", FamilyRole.OWNER.name())
                .addValue("parentRole", FamilyRole.PARENT.name());

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> FamilyListItem.builder()
                .familyId(rs.getLong("family_id"))
                .representativeName(rs.getString("representative_name"))
                .phoneNumber(rs.getString("phone_number_enc"))
                .memberCount(rs.getInt("member_count"))
                .usedData(null)
                .remainingData(null)
                .build());
    }
}
