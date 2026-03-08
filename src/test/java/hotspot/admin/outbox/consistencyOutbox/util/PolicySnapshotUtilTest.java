package hotspot.admin.outbox.consistencyOutbox.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hotspot.admin.policy.domain.PolicyDay;
import hotspot.admin.policy.domain.PolicyPayload;
import hotspot.admin.policy.domain.PolicySnapshot;
import hotspot.admin.policy.domain.PolicyType;

class PolicySnapshotUtilTest {

    @Test
    @DisplayName("SCHEDULED 정책이면 요일/시작시간/종료시간을 인코딩한 payload를 만든다")
    void mapScheduledPolicy() {
        PolicySnapshot snapshot = PolicySnapshot.builder()
                .days(List.of(PolicyDay.MON, PolicyDay.TUE))
                .startTime("06:00")
                .endTime("07:00")
                .build();

        PolicyPayload payload = PolicySnapshotUtil.map(1L, PolicyType.SCHEDULED, snapshot);

        assertThat(payload.policyId()).isEqualTo(1L);
        assertThat(payload.encoded()).isEqualTo("1,2|06:00|07:00");
    }

    @Test
    @DisplayName("ONCE 정책에서 durationMinutes가 있으면 현재 시각 기준 만료 epoch를 만든다")
    void mapOncePolicyWithDurationMinutes() {
        PolicySnapshot snapshot = PolicySnapshot.builder()
                .durationMinutes(30)
                .build();

        long before = Instant.now().getEpochSecond();

        PolicyPayload payload = PolicySnapshotUtil.map(2L, PolicyType.ONCE, snapshot);

        long after = Instant.now().plusSeconds(30 * 60).getEpochSecond();

        assertThat(payload.policyId()).isEqualTo(2L);
        assertThat(payload.expireEpoch()).isBetween(before + 30 * 60, after);
    }

    @Test
    @DisplayName("ONCE 정책에서 endTime이 있으면 종료 시각 기준 만료 epoch를 만든다")
    void mapOncePolicyWithEndTime() {
        PolicySnapshot snapshot = PolicySnapshot.builder()
                .endTime("23:59")
                .build();

        PolicyPayload payload = PolicySnapshotUtil.map(3L, PolicyType.ONCE, snapshot);

        assertThat(payload.policyId()).isEqualTo(3L);
        assertThat(payload.expireEpoch()).isGreaterThan(0);
    }

    @Test
    @DisplayName("ONCE 정책인데 durationMinutes와 endTime이 모두 없으면 예외가 발생한다")
    void mapOncePolicyInvalidSnapshot() {
        PolicySnapshot snapshot = PolicySnapshot.builder().build();

        assertThatThrownBy(() -> PolicySnapshotUtil.map(4L, PolicyType.ONCE, snapshot))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid ONCE policy snapshot");
    }

    @Test
    @DisplayName("알 수 없는 정책 타입이면 예외가 발생한다")
    void mapUnknownPolicyType() {
        PolicySnapshot snapshot = PolicySnapshot.builder().build();

        assertThatThrownBy(() -> PolicySnapshotUtil.map(5L, null, snapshot))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unknown policy type");
    }
}
