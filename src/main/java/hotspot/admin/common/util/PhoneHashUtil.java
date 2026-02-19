package hotspot.admin.common.util;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PhoneHashUtil {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^01\\d{9}$");
    private final List<byte[]> keyCandidates;

    public PhoneHashUtil(@Value("${project.phone-hash-key}") String hashKey) {
        this.keyCandidates = resolveKeyCandidates(hashKey);
    }

    public String hashPhone(String rawPhoneNumber) throws GeneralSecurityException {
        String normalizedPhone = normalizePhone(rawPhoneNumber);

        for (byte[] keyBytes : keyCandidates) {
            try {
                Mac mac = Mac.getInstance(HMAC_ALGORITHM);
                mac.init(new SecretKeySpec(keyBytes, HMAC_ALGORITHM));
                byte[] signature = mac.doFinal(normalizedPhone.getBytes(StandardCharsets.UTF_8));
                return Base64.getEncoder().encodeToString(signature);
            } catch (GeneralSecurityException e) {
                log.trace("Phone hash generation failed with current key candidate.", e);
            }
        }

        throw new GeneralSecurityException("Unable to hash phone with configured key.");
    }

    public String normalizePhone(String rawPhoneNumber) {
        if (rawPhoneNumber == null || rawPhoneNumber.isBlank()) {
            throw new IllegalArgumentException("Phone number is blank.");
        }

        String digits = rawPhoneNumber.replaceAll("\\D", "");
        if (!MOBILE_PATTERN.matcher(digits).matches()) {
            throw new IllegalArgumentException("Phone number format is invalid.");
        }

        return digits.substring(0, 3) + "-" + digits.substring(3, 7) + "-" + digits.substring(7);
    }

    private List<byte[]> resolveKeyCandidates(String rawKey) {
        List<byte[]> candidates = new ArrayList<>();

        try {
            byte[] decoded = Base64.getDecoder().decode(rawKey);
            addIfValidKey(candidates, decoded);
        } catch (IllegalArgumentException e) {
            log.trace("PHONE_HASH_KEY is not Base64 encoded. Raw key bytes only will be considered.");
        }

        addIfValidKey(candidates, rawKey.getBytes(StandardCharsets.UTF_8));

        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("PHONE_HASH_KEY must not be empty.");
        }

        return candidates;
    }

    private void addIfValidKey(List<byte[]> candidates, byte[] key) {
        if (key.length > 0) {
            candidates.add(key);
        }
    }
}
