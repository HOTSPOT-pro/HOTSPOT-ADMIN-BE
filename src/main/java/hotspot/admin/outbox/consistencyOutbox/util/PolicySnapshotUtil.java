package hotspot.admin.outbox.consistencyOutbox.util;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import hotspot.admin.policy.domain.PolicyDay;
import hotspot.admin.policy.domain.PolicyPayload;
import hotspot.admin.policy.domain.PolicySnapshot;
import hotspot.admin.policy.domain.PolicyType;

public class PolicySnapshotUtil {

    // 유틸리티 클래스이므로 인스턴스 생성을 방지한다.
    private PolicySnapshotUtil() {}

    /**
     * 정책 도메인 정보를 Worker에서 사용할 수 있는 PolicyPayload 형태로 변환한다.
     * 이렇게 생성된 PolicyPayload는 이후 Outbox → Kafka → Worker → Redis 정책 적용 흐름에서 사용.
     */
    public static PolicyPayload map(
            Long policyId,
            PolicyType type,
            PolicySnapshot snapshot
    ) {

        if (type == PolicyType.SCHEDULED) {

            String encoded =
                    encodeDays(snapshot.getDays())
                            + "|" + snapshot.getStartTime()
                            + "|" + snapshot.getEndTime();

            return PolicyPayload.scheduled(policyId, encoded);
        }

        if (type == PolicyType.ONCE) {

            long expireEpoch = calculateExpireEpoch(snapshot);

            return PolicyPayload.once(policyId, expireEpoch);
        }

        throw new IllegalArgumentException("Unknown policy type");
    }

    /**
     * 요일 리스트를 Worker에서 사용할 수 있는 문자열 형태로 변환한다.
     * 이 값은 이후 "요일|시작시간|종료시간" 형태의 정책 문자열을 구성할 때 사용
     */
    private static String encodeDays(List<PolicyDay> days) {

        return days.stream()
                .map(day -> String.valueOf(day.getValue()))
                .collect(Collectors.joining(","));
    }

    /**
     * ONCE 정책의 만료 시점을 epoch(second) 기준으로 계산한다.
     * 1. durationMinutes 기반 정책
     *    현재 시각(now)에 durationMinutes를 더해 만료 시간을 계산한다.
     *
     * 2. endTime 기반 정책
     *    오늘 날짜 + endTime으로 종료 시점을 계산한다.
     *    만약 해당 시간이 이미 지난 경우에는 다음 날 동일한 시간으로 보정한다.
     * 계산된 LocalDateTime은 Worker와 Redis Lua에서 사용하기 위해 UTC 기준 epoch(second) 값으로 변환된다.
     */
    private static long calculateExpireEpoch(PolicySnapshot snapshot) {

        Instant now = Instant.now();

        if (snapshot.getDurationMinutes() != null) {

            return now.plus(Duration.ofMinutes(snapshot.getDurationMinutes()))
                    .getEpochSecond();
        }

        if (snapshot.getEndLocalTime() != null) {

            ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);

            ZonedDateTime endDateTime =
                    snapshot.getEndLocalTime()
                            .atDate(nowUtc.toLocalDate())
                            .atZone(ZoneOffset.UTC);

            if (endDateTime.isBefore(nowUtc)) {
                endDateTime = endDateTime.plusDays(1);
            }

            return endDateTime.toEpochSecond();
        }

        throw new IllegalArgumentException("Invalid ONCE policy snapshot");
    }
}
