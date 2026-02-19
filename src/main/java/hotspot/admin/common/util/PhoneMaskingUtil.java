package hotspot.admin.common.util;

public final class PhoneMaskingUtil {

    private PhoneMaskingUtil() {
    }

    public static String maskMiddle(String phone) {
        if (phone == null || phone.isBlank()) {
            return phone;
        }

        String digits = phone.replaceAll("\\D", "");
        if (digits.length() == 11) {
            return digits.substring(0, 3) + "-****-" + digits.substring(7);
        }
        if (digits.length() == 10) {
            return digits.substring(0, 3) + "-***-" + digits.substring(6);
        }
        if (digits.length() > 4) {
            int prefix = Math.min(3, digits.length() / 3);
            int suffix = Math.min(4, digits.length() - prefix);
            StringBuilder sb = new StringBuilder();
            sb.append(digits, 0, prefix).append("-");
            sb.append("*".repeat(Math.max(1, digits.length() - prefix - suffix))).append("-");
            sb.append(digits.substring(digits.length() - suffix));
            return sb.toString();
        }

        return digits;
    }
}
