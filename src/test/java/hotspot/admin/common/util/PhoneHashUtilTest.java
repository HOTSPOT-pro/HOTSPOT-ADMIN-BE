package hotspot.admin.common.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PhoneHashUtilTest {

    @Test
    @DisplayName("Base64 인코딩된 PHONE_HASH_KEY를 디코딩해서 HMAC 해시를 생성한다")
    void hashPhoneUsesDecodedBase64Key() throws Exception {
        String base64Key = "AuUur/os0s5+DXF9tB6DKOzJbJwl3WIY1ooEYMLwHmc=";
        String phone = "010-0000-0000";
        PhoneHashUtil util = new PhoneHashUtil(base64Key);

        String actual = util.hashPhone(phone);
        String expected = expectedHash(base64Key, phone);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("전화번호 11자리를 하이픈 포함 형식으로 정규화한다")
    void normalizePhoneElevenDigits() {
        PhoneHashUtil util = new PhoneHashUtil("dummy-key");
        String normalized = util.normalizePhone("01012345678");
        assertThat(normalized).isEqualTo("010-1234-5678");
    }

    @Test
    @DisplayName("전화번호 10자리를 하이픈 포함 형식으로 정규화한다")
    void normalizePhoneTenDigits() {
        PhoneHashUtil util = new PhoneHashUtil("dummy-key");
        String normalized = util.normalizePhone("0101234567");
        assertThat(normalized).isEqualTo("010-123-4567");
    }

    @Test
    @DisplayName("전화번호 형식이 맞지 않으면 예외를 던진다")
    void normalizePhoneInvalidFormat() {
        PhoneHashUtil util = new PhoneHashUtil("dummy-key");
        assertThatThrownBy(() -> util.normalizePhone("0212345678"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private String expectedHash(String base64Key, String phone) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(base64Key);
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(decodedKey, "HmacSHA256"));
        byte[] signature = mac.doFinal(phone.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signature);
    }
}
