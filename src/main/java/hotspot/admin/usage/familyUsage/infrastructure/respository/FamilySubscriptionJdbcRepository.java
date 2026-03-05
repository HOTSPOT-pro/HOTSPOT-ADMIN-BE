package hotspot.admin.usage.familyUsage.infrastructure.respository;

import hotspot.admin.usage.familyUsage.service.schema.FamilySubList;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

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
