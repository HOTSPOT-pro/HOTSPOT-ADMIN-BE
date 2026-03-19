package hotspot.admin.policy.util;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import hotspot.admin.policy.domain.PolicyDay;
import hotspot.admin.policy.domain.PolicySnapshot;

public final class PolicyScheduleLabelFormatter {

    private static final Set<PolicyDay> WEEKDAYS = EnumSet.of(
            PolicyDay.MONDAY,
            PolicyDay.TUESDAY,
            PolicyDay.WEDNESDAY,
            PolicyDay.THURSDAY,
            PolicyDay.FRIDAY
    );
    private static final Set<PolicyDay> EVERYDAY = EnumSet.allOf(PolicyDay.class);
    private static final Set<PolicyDay> WEEKEND = EnumSet.of(PolicyDay.SATURDAY, PolicyDay.SUNDAY);

    private PolicyScheduleLabelFormatter() {
    }

    public static String toPolicyScheduleLabel(PolicySnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }

        Integer durationMinutes = snapshot.getDurationMinutes();
        if (durationMinutes != null && durationMinutes > 0) {
            if (durationMinutes % 60 == 0) {
                return (durationMinutes / 60) + "시간";
            }
            return durationMinutes + "분";
        }

        String startTime = snapshot.getStartTime();
        String endTime = snapshot.getEndTime();
        if (startTime == null || endTime == null) {
            return null;
        }

        List<PolicyDay> days = snapshot.getDays();
        if (days == null || days.isEmpty()) {
            return startTime + "~" + endTime;
        }

        Set<PolicyDay> daySet = EnumSet.copyOf(days);
        if (daySet.equals(EVERYDAY)) {
            return "매일 " + startTime + "~" + endTime;
        }
        if (daySet.equals(WEEKDAYS)) {
            return "주중 " + startTime + "~" + endTime;
        }
        if (daySet.equals(WEEKEND)) {
            return "주말 " + startTime + "~" + endTime;
        }

        String dayLabel = daySet.stream()
                .sorted()
                .map(PolicyScheduleLabelFormatter::toKoreanDayShort)
                .collect(Collectors.joining(","));
        return dayLabel + " " + startTime + "~" + endTime;
    }

    private static String toKoreanDayShort(PolicyDay day) {
        return switch (day) {
            case MONDAY -> "월";
            case TUESDAY -> "화";
            case WEDNESDAY -> "수";
            case THURSDAY -> "목";
            case FRIDAY -> "금";
            case SATURDAY -> "토";
            case SUNDAY -> "일";
        };
    }
}
