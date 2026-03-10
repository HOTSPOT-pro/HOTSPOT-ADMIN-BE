package hotspot.admin.common.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import hotspot.admin.subscription.service.port.SubscriptionPhoneKeyLookupRepository;
import hotspot.admin.subscription.service.port.dto.SubscriptionPhoneKeyInfo;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DecryptRequest;
import software.amazon.awssdk.services.kms.model.KmsException;

@Component
@Slf4j
public class PhoneCryptoUtil implements DisposableBean {

    private static final String AES_ALGORITHM = "AES";
    private static final int AES_BLOCK_SIZE = 16;
    private static final int AES_KEY_SIZE_128 = 16;
    private static final int AES_KEY_SIZE_192 = 24;
    private static final int AES_KEY_SIZE_256 = 32;
    private static final int GCM_NONCE_SIZE = 12;
    private static final int GCM_TAG_SIZE = 16;
    private static final String GCM_PREFIX = "gcm:";
    private static final String ENCRYPTION_PROVIDER_LOCAL = "local";
    private static final String ENCRYPTION_PROVIDER_KMS = "kms";

    private final SubscriptionPhoneKeyLookupRepository phoneKeyLookupRepository;
    private final byte[] localSecretKey;
    private final String encryptionProvider;
    private final String configuredKmsKeyId;

    private volatile KmsClient kmsClient;

    public PhoneCryptoUtil(
            SubscriptionPhoneKeyLookupRepository phoneKeyLookupRepository,
            @Value("${project.phone-secret-key}") String phoneSecretKey,
            @Value("${project.encryption-provider:local}") String encryptionProvider,
            @Value("${project.kms-key-id:}") String configuredKmsKeyId
    ) {
        this.phoneKeyLookupRepository = phoneKeyLookupRepository;
        this.localSecretKey = parseAndValidateSecretKey(phoneSecretKey);
        this.encryptionProvider = encryptionProvider == null
                ? ENCRYPTION_PROVIDER_LOCAL
                : encryptionProvider.trim().toLowerCase();
        this.configuredKmsKeyId = configuredKmsKeyId == null ? "" : configuredKmsKeyId.trim();
    }

    public String decryptPhone(String encryptedPhone, Long subId) throws GeneralSecurityException {
        if (encryptedPhone == null || encryptedPhone.isBlank()) {
            return encryptedPhone;
        }
        if (subId == null) {
            throw new GeneralSecurityException("subId is required to decrypt phone.");
        }

        SubscriptionPhoneKeyInfo phoneKeyInfo = phoneKeyLookupRepository.findPhoneKeyInfoBySubId(subId)
                .orElseThrow(() -> new GeneralSecurityException("Phone key info not found for subId=" + subId));

        byte[] dek = unwrapDek(phoneKeyInfo);
        return decryptPayload(encryptedPhone, dek);
    }

    private byte[] unwrapDek(SubscriptionPhoneKeyInfo phoneKeyInfo) throws GeneralSecurityException {
        if (ENCRYPTION_PROVIDER_KMS.equals(encryptionProvider)) {
            return decryptDekWithKms(phoneKeyInfo);
        }
        return decryptPayloadBytes(phoneKeyInfo.encryptedDek(), localSecretKey);
    }

    private byte[] decryptDekWithKms(SubscriptionPhoneKeyInfo phoneKeyInfo) throws GeneralSecurityException {
        String kmsKeyId = phoneKeyInfo.kekKeyId();
        if (kmsKeyId == null || kmsKeyId.isBlank()) {
            kmsKeyId = configuredKmsKeyId;
        }

        try {
            DecryptRequest.Builder requestBuilder = DecryptRequest.builder()
                    .ciphertextBlob(SdkBytes.fromByteArray(Base64.getDecoder().decode(phoneKeyInfo.encryptedDek())));
            if (kmsKeyId != null && !kmsKeyId.isBlank()) {
                requestBuilder.keyId(kmsKeyId);
            }
            return getKmsClient().decrypt(requestBuilder.build()).plaintext().asByteArray();
        } catch (IllegalArgumentException | KmsException e) {
            log.warn("KMS DEK decrypt failed for subId={}", phoneKeyInfo.subId(), e);
            throw new GeneralSecurityException("Failed to decrypt DEK with KMS.", e);
        }
    }

    private String decryptPayload(String cipherText, byte[] key) throws GeneralSecurityException {
        return new String(decryptPayloadBytes(cipherText, key), StandardCharsets.UTF_8);
    }

    private byte[] decryptPayloadBytes(String cipherText, byte[] key) throws GeneralSecurityException {
        if (cipherText == null || cipherText.isBlank()) {
            throw new GeneralSecurityException("Cipher text must not be null or blank.");
        }
        try {
            if (cipherText.startsWith(GCM_PREFIX)) {
                String encoded = cipherText.substring(GCM_PREFIX.length());
                byte[] decoded = Base64.getDecoder().decode(encoded);
                return decryptAesGcm(decoded, key);
            }
            return decryptAesCbc(Base64.getDecoder().decode(cipherText), key);
        } catch (IllegalArgumentException e) {
            throw new GeneralSecurityException("Invalid encrypted payload.", e);
        }
    }

    private byte[] decryptAesGcm(byte[] payload, byte[] key) throws GeneralSecurityException {
        if (payload.length <= GCM_NONCE_SIZE + GCM_TAG_SIZE) {
            throw new GeneralSecurityException("Invalid AES/GCM payload.");
        }
        byte[] nonce = Arrays.copyOfRange(payload, 0, GCM_NONCE_SIZE);
        byte[] encrypted = Arrays.copyOfRange(payload, GCM_NONCE_SIZE, payload.length - GCM_TAG_SIZE);
        byte[] tag = Arrays.copyOfRange(payload, payload.length - GCM_TAG_SIZE, payload.length);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(
                Cipher.DECRYPT_MODE,
                new SecretKeySpec(key, AES_ALGORITHM),
                new GCMParameterSpec(GCM_TAG_SIZE * 8, nonce)
        );
        return cipher.doFinal(ByteBuffer.allocate(encrypted.length + tag.length).put(encrypted).put(tag).array());
    }

    private byte[] decryptAesCbc(byte[] payload, byte[] key) throws GeneralSecurityException {
        if (payload.length <= AES_BLOCK_SIZE) {
            throw new GeneralSecurityException("Invalid AES/CBC payload.");
        }
        byte[] iv = Arrays.copyOfRange(payload, 0, AES_BLOCK_SIZE);
        byte[] ciphertext = Arrays.copyOfRange(payload, AES_BLOCK_SIZE, payload.length);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, AES_ALGORITHM), new IvParameterSpec(iv));
        return cipher.doFinal(ciphertext);
    }

    private byte[] parseAndValidateSecretKey(String rawKey) {
        Objects.requireNonNull(rawKey, "PHONE_SECRET_KEY must not be null.");
        byte[] rawBytes = rawKey.getBytes(StandardCharsets.UTF_8);
        if (isValidAesKeyLength(rawBytes.length)) {
            return rawBytes;
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(rawKey);
            if (isValidAesKeyLength(decoded.length)) {
                return decoded;
            }
        } catch (IllegalArgumentException e) {
            log.trace("PHONE_SECRET_KEY is not Base64 encoded.");
        }

        throw new IllegalArgumentException("PHONE_SECRET_KEY length must be 16/24/32 bytes.");
    }

    private boolean isValidAesKeyLength(int length) {
        return length == AES_KEY_SIZE_128
                || length == AES_KEY_SIZE_192
                || length == AES_KEY_SIZE_256;
    }

    private KmsClient getKmsClient() {
        KmsClient current = kmsClient;
        if (current != null) {
            return current;
        }
        synchronized (this) {
            if (kmsClient == null) {
                kmsClient = KmsClient.builder().build();
            }
            return kmsClient;
        }
    }

    @Override
    public void destroy() {
        KmsClient current = kmsClient;
        if (current != null) {
            current.close();
        }
    }
}
