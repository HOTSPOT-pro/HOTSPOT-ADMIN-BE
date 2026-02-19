package hotspot.admin.common.util;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PhoneCryptoUtil {

    private static final int AES_BLOCK_SIZE = 16;
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
                } catch (GeneralSecurityException ignored) {
                    // Fallback to ECB if storage format is ciphertext only.
                }
            }

            try {
                return decryptAesEcb(decoded, keyBytes);
            } catch (GeneralSecurityException ignored) {
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
        byte[] rawBytes = rawKey.getBytes(StandardCharsets.UTF_8);
        byte[] normalizedRaw = normalizeKey(rawBytes);

        try {
            byte[] decoded = Base64.getDecoder().decode(rawKey);
            byte[] normalizedDecoded = normalizeKey(decoded);
            return List.of(normalizedDecoded, normalizedRaw);
        } catch (IllegalArgumentException ignored) {
            return List.of(normalizedRaw);
        }
    }

    private byte[] normalizeKey(byte[] source) {
        if (source.length == 16 || source.length == 24 || source.length == 32) {
            return source;
        }
        byte[] normalized = new byte[32];
        int length = Math.min(source.length, normalized.length);
        System.arraycopy(source, 0, normalized, 0, length);
        return normalized;
    }
}
