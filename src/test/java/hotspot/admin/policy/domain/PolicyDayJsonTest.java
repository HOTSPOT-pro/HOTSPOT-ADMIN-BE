package hotspot.admin.policy.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class PolicyDayJsonTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("요일 축약형 입력은 허용하고 직렬화는 풀네임으로 한다")
    void policyDayUsesFullNameAsCanonicalJsonValue() throws Exception {
        PolicySnapshot snapshot = objectMapper.readValue(
                """
                {
                  "days": ["MON", "TUE"],
                  "startTime": "06:00",
                  "endTime": "07:00"
                }
                """,
                PolicySnapshot.class
        );

        assertThat(snapshot.getDays()).containsExactly(PolicyDay.MONDAY, PolicyDay.TUESDAY);
        assertThat(objectMapper.writeValueAsString(snapshot))
                .contains("\"days\":[\"MONDAY\",\"TUESDAY\"]");
    }

    @Test
    @DisplayName("풀네임 입력도 정상 역직렬화한다")
    void policyDayAcceptsFullNameInput() throws Exception {
        PolicySnapshot snapshot = objectMapper.readValue(
                """
                {
                  "days": ["MONDAY", "SUNDAY"]
                }
                """,
                PolicySnapshot.class
        );

        assertThat(snapshot.getDays()).isEqualTo(List.of(PolicyDay.MONDAY, PolicyDay.SUNDAY));
    }
}
