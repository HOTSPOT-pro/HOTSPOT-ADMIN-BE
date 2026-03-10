package hotspot.admin.subscription.service.port;

import java.util.Optional;

import hotspot.admin.subscription.service.port.dto.SubscriptionPhoneKeyInfo;

public interface SubscriptionPhoneKeyLookupRepository {
    Optional<SubscriptionPhoneKeyInfo> findPhoneKeyInfoBySubId(Long subId);
}
