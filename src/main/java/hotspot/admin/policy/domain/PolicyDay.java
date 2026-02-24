package hotspot.admin.policy.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PolicyDay {
    MON,
    TUE,
    WED,
    THU,
    FRI,
    SAT,
    SUN;

    @JsonCreator
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
