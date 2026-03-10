package hotspot.admin.subscription.service.port.dto;

import lombok.Builder;

@Builder
public record SubscriptionPhoneKeyInfo(
        Long subId,
        Integer bucketId,
        Integer keyVersion,
        String encryptedDek,
        String kekKeyId,
        String status
) {
}
