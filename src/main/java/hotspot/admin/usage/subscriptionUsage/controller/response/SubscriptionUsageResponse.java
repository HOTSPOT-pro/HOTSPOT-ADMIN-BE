package hotspot.admin.usage.subscriptionUsage.controller.response;

import hotspot.admin.family.domain.FamilyRole;

import java.time.LocalDateTime;
import java.util.List;

public record SubscriptionUsageResponse(
        Long subId,
        LocalDateTime currentTime,
        String subName,
        FamilyRole familyRole,
        String phoneEnc,
        String planName, // 요금제 이름
        Double subDataAmount,
        Double subDataUsageAmount,
        Double subDataRemainAmount,
        Integer dataUsagePercent,
        Double giftDataAmount,
        Double giftDataUsageAmount,
        Double giftDataRemainAmount,
        Integer giftUsagePercent,
        List<GiftUsageResponse> giftUsages
) {

    public record GiftUsageResponse(
            Long giftId,
            String giftUserName,
            Double giftDataLimit,
            Double giftDataUsageAmount,
            Double giftDataUsageRemainAmount,
            Integer dataUsagePercent
    ) {
    }
}
