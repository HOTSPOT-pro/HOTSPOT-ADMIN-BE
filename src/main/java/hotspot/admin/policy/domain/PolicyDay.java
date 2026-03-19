package hotspot.admin.policy.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PolicyDay {
    MONDAY(1),
    TUESDAY(2),
    WEDNESDAY(3),
    THURSDAY(4),
    FRIDAY(5),
    SATURDAY(6),
    SUNDAY(7);

    private final int value;

    PolicyDay(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static PolicyDay from(String value) {
        if (value == null) {
            return null;
        }

        return switch (value.trim().toUpperCase()) {
            case "MON", "MONDAY" -> MONDAY;
            case "TUE", "TUESDAY" -> TUESDAY;
            case "WED", "WEDNESDAY" -> WEDNESDAY;
            case "THU", "THURSDAY" -> THURSDAY;
            case "FRI", "FRIDAY" -> FRIDAY;
            case "SAT", "SATURDAY" -> SATURDAY;
            case "SUN", "SUNDAY" -> SUNDAY;
            default -> throw new IllegalArgumentException("Unknown day: " + value);
        };
    }
}
