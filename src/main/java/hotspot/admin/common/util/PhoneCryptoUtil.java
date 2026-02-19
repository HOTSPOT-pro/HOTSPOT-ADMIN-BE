package hotspot.admin.common.util;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PhoneCryptoUtil {

    private static final int AES_BLOCK_SIZE = 16;
    private static final int AES_KEY_SIZE_128 = 16;
    private static final int AES_KEY_SIZE_192 = 24;
    private static final int AES_KEY_SIZE_256 = 32;
    private static final String AES_ALGORITHM = "AES";

    private final List<byte[]> keyCandidates;

    public PhoneCryptoUtil(@Value("${project.phone-secret-key}") String encryptionKey) {
        this.keyCandidates = resolveKeyCandidates(encryptionKey);
    }

    public String decryptPhone(String encryptedPhone) throws GeneralSecurityException {
        byte[] decoded = Base64.getDecoder().decode(encryptedPhone);

        for (byte[] keyBytes : keyCandidates) {
            // Try IV + ciphertext format first.
            if (decoded.length > AES_BLOCK_SIZE) {
                byte[] iv = Arrays.copyOfRange(decoded, 0, AES_BLOCK_SIZE);
                byte[] ciphertext = Arrays.copyOfRange(decoded, AES_BLOCK_SIZE, decoded.length);
                try {
                    return decryptAesCbc(ciphertext, iv, keyBytes);
                } catch (GeneralSecurityException e) {
                    log.trace("CBC decryption failed. Falling back to ECB mode.", e);
                    // Fallback to ECB if storage format is ciphertext only.
                }
            }

            try {
                return decryptAesEcb(decoded, keyBytes);
            } catch (GeneralSecurityException e) {
                log.trace("ECB decryption failed with current key candidate.", e);
                // Try next key candidate.
            }
        }

        throw new GeneralSecurityException("Unable to decrypt phone with configured key.");
    }

    private String decryptAesCbc(byte[] ciphertext, byte[] iv, byte[] keyBytes) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, AES_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));
        return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
    }

    private String decryptAesEcb(byte[] ciphertext, byte[] keyBytes) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, AES_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
    }

    private List<byte[]> resolveKeyCandidates(String rawKey) {
        List<byte[]> candidates = new ArrayList<>();

        addIfValidKey(candidates, rawKey.getBytes(StandardCharsets.UTF_8));

        try {
            byte[] decoded = Base64.getDecoder().decode(rawKey);
            addIfValidKey(candidates, decoded);
        } catch (IllegalArgumentException e) {
            log.trace("PHONE_SECRET_KEY is not Base64 encoded. Raw key bytes only will be considered.");
        }

        if (candidates.isEmpty()) {
            throw new IllegalArgumentException(
                    "PHONE_SECRET_KEY length must be 16/24/32 bytes (raw or Base64-decoded).");
        }

        return candidates;
    }

    private void addIfValidKey(List<byte[]> candidates, byte[] key) {
        if (isValidAesKeyLength(key.length)) {
            candidates.add(key);
        }
    }

    private boolean isValidAesKeyLength(int length) {
        return length == AES_KEY_SIZE_128 || length == AES_KEY_SIZE_192 || length == AES_KEY_SIZE_256;
    }
}
