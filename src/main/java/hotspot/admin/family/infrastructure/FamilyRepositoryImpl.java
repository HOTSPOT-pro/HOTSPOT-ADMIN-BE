package hotspot.admin.family.infrastructure;

import java.util.List;
import java.util.Optional;

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

    private FamilyListItem mapFamilyListItem(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return FamilyListItem.builder()
                .familyId(rs.getLong("family_id"))
                .representativeName(rs.getString("representative_name"))
                .phoneNumber(rs.getString("phone_number_enc"))
                .memberCount(rs.getInt("member_count"))
                .build();
    }
}
