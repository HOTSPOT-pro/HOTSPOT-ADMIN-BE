package hotspot.admin.policy.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PolicyDay {
    MON(1),
    TUE(2),
    WED(3),
    THU(4),
    FRI(5),
    SAT(6),
    SUN(7);

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
            case "MON", "MONDAY" -> MON;
            case "TUE", "TUESDAY" -> TUE;
            case "WED", "WEDNESDAY" -> WED;
            case "THU", "THURSDAY" -> THU;
            case "FRI", "FRIDAY" -> FRI;
            case "SAT", "SATURDAY" -> SAT;
            case "SUN", "SUNDAY" -> SUN;
            default -> throw new IllegalArgumentException("Unknown day: " + value);
        };
    }
}
