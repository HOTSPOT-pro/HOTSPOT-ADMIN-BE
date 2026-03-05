package hotspot.admin.common.util.redis;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class RedisValueParser {

    public static double toDouble(Object value) {

        if (value == null) {
            return 0D;
        }

        try {
            if (value instanceof byte[] bytes) {
                return safeParseDouble(new String(bytes, StandardCharsets.UTF_8));
            }
            return safeParseDouble(value.toString());
        } catch (Exception e) {
            log.warn("Invalid Redis double value: {}", value);
            return 0D;
        }
    }

    public static Long toLong(Object value) {

        if (value == null) {
            return null;
        }

        try {
            if (value instanceof byte[] bytes) {
                return safeParseLong(new String(bytes, StandardCharsets.UTF_8));
            }
            return safeParseLong(value.toString());
        } catch (Exception e) {
            log.warn("Invalid Redis long value: {}", value);
            return null;
        }
    }

    private static double safeParseDouble(String value) {
        try {
            double parsed = Double.parseDouble(value);
            if (Double.isNaN(parsed) || Double.isInfinite(parsed)) {
                return 0D;
            }
            return parsed;
        } catch (NumberFormatException e) {
            log.warn("Failed to parse double: {}", value);
            return 0D;
        }
    }

    private static Long safeParseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse long: {}", value);
            return null;
        }
    }
}
