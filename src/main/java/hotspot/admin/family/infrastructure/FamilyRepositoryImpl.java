package hotspot.admin.family.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import hotspot.admin.family.controller.response.FamilyListItem;
import hotspot.admin.family.domain.FamilyRole;
import hotspot.admin.family.service.port.FamilyQueryRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class FamilyRepositoryImpl implements FamilyQueryRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    /** 가족 목록 화면용 상단 정보(대표자/전화/인원수)를 페이지 단위로 조회한다. */
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

    /** 삭제되지 않은 가족 총 건수를 조회한다. */
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

    /** 전화번호 해시로 가족 1건을 검색한다. */
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

    /** 가족 상세 상단 표시용 요약 정보를 가족 ID로 조회한다. */
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

    /** JDBC 결과 행을 가족 목록/상단 응답 행으로 변환한다. */
    private FamilyListItem mapFamilyListItem(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return FamilyListItem.builder()
                .familyId(rs.getLong("family_id"))
                .representativeName(rs.getString("representative_name"))
                .phoneNumber(rs.getString("phone_number_enc"))
                .memberCount(rs.getInt("member_count"))
                .build();
    }
}
