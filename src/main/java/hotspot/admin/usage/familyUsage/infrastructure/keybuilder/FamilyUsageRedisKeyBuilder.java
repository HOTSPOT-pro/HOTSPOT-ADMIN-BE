package hotspot.admin.usage.familyUsage.infrastructure.keybuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FamilyUsageRedisKeyBuilder {

    private static final DateTimeFormatter yyyyMM =
            DateTimeFormatter.ofPattern("yyyyMM");

    public static String familyLimit(Long familyId) {
        return "limit:family:" + familyId;
    }

    public static String familySubLimit(Long familyId, Long subId) {
        return "limit:family_sub:" + familyId + ":" + subId;
    }

    public static String familyUsage(Long familyId, LocalDate date) {
        return "usage:family:" + familyId + ":" + format(date);
    }

    public static String subUsage(Long subId, LocalDate date) {
        return "usage:sub:" + subId + ":" + format(date);
    }

    private static String format(LocalDate date) {
        return date.format(yyyyMM);
    }
}
