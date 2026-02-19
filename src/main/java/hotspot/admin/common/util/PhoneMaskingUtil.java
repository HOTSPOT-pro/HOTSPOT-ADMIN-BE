package hotspot.admin.common.util;

public final class PhoneMaskingUtil {

    private static final int MOBILE_LENGTH_11 = 11;
    private static final int MOBILE_LENGTH_10 = 10;
    private static final int PREFIX_LENGTH = 3;
    private static final int SUFFIX_LENGTH_11 = 4;
    private static final int SUFFIX_START_11 = 7;
    private static final int SUFFIX_START_10 = 6;
    private static final int MIN_FALLBACK_LENGTH = 4;
    private static final int MAX_DYNAMIC_SUFFIX = 4;

    private PhoneMaskingUtil() {
    }

    public static String maskMiddle(String phone) {
        if (phone == null || phone.isBlank()) {
            return phone;
        }

        String digits = phone.replaceAll("\\D", "");
        if (digits.length() == MOBILE_LENGTH_11) {
            return digits.substring(0, PREFIX_LENGTH) + "-****-" + digits.substring(SUFFIX_START_11);
        }
        if (digits.length() == MOBILE_LENGTH_10) {
            return digits.substring(0, PREFIX_LENGTH) + "-***-" + digits.substring(SUFFIX_START_10);
        }
        if (digits.length() > MIN_FALLBACK_LENGTH) {
            int prefix = Math.min(PREFIX_LENGTH, digits.length() / 3);
            int suffix = Math.min(MAX_DYNAMIC_SUFFIX, digits.length() - prefix);
            StringBuilder sb = new StringBuilder();
            sb.append(digits, 0, prefix).append("-");
            sb.append("*".repeat(Math.max(1, digits.length() - prefix - suffix))).append("-");
            sb.append(digits.substring(digits.length() - suffix));
            return sb.toString();
        }

        return digits;
    }
}
