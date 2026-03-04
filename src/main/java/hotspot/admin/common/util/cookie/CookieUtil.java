package hotspot.admin.common.util.cookie;

import org.springframework.http.ResponseCookie;

public final class CookieUtil {

    private CookieUtil() {}

    public static ResponseCookie createCookie(String name, String value, long maxAgeMillis) {
        return ResponseCookie.from(name, value)
                // 로컬에서는 필요 시 주석처리
                .domain(".hotspot.pics")
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .maxAge(maxAgeMillis / 1000)
                .build();
    }
}
