package hotspot.admin.usage.familyUsage.infrastructure.respository;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import hotspot.admin.usage.familyUsage.service.schema.FamilySubList;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class FamilySubscriptionJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<FamilySubList> findByFamilyId(Long familyId) {

        String sql = """
                    SELECT
                        fs.sub_id,
                        m.name
                    FROM family_sub fs
                    JOIN subscription s
                        ON fs.sub_id = s.sub_id
                    JOIN member m
                        ON s.member_id = m.member_id
                    WHERE fs.family_id = ?
                    ORDER BY
                        CASE fs.family_role
                            WHEN 'OWNER' THEN 1
                            WHEN 'PARENT' THEN 2
                            WHEN 'CHILD' THEN 3
                            ELSE 4
                        END
                    """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new FamilySubList(
                        rs.getLong("sub_id"),
                        rs.getString("name")
                ),
                familyId
        );
    }
}
