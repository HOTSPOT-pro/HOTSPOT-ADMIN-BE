package hotspot.admin.usage.subscriptionUsage.infrastructure.repository;


import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class PresentDataJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public Map<Long, String> findGiftGiverNames(List<Long> giftIds) {

        if (giftIds == null || giftIds.isEmpty()) {
            return Map.of();
        }

        String sql = """
            SELECT
                pd.present_data_id,
                m.name
            FROM present_data pd
            JOIN subscription s
                ON pd.provide_sub_id = s.sub_id
            JOIN member m
                ON s.member_id = m.member_id
            WHERE pd.present_data_id IN (%s)
            """.formatted(
                giftIds.stream()
                        .map(id -> "?")
                        .reduce((a,b)->a+","+b)
                        .orElse("")
        );

        List<Map<String, Object>> rows =
                jdbcTemplate.queryForList(sql, giftIds.toArray());

        Map<Long, String> result = new HashMap<>();

        for (Map<String, Object> row : rows) {

            Long giftId = ((Number) row.get("present_data_id")).longValue();
            String name = (String) row.get("name");

            result.put(giftId, name);
        }

        return result;
    }
}
