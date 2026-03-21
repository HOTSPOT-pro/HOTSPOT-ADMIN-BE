package hotspot.admin.usage.subscriptionUsage.domain.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import hotspot.admin.family.domain.FamilyRole;
import hotspot.admin.subscription.domain.Subscription;
import hotspot.admin.usage.subscriptionUsage.controller.response.SubscriptionUsageResponse;
import hotspot.admin.usage.subscriptionUsage.domain.SubscriptionUsage;

public class SubscriptionUsageMapper {

    public static SubscriptionUsageResponse toSubscriptionUsageResponse(
            SubscriptionUsage usage,
            String maskedPhone,
            FamilyRole familyRole,
            Subscription subscription,
            Boolean blocked,
            Map<Long, String> giftIdToUserName,
            LocalDateTime now
    ) {

        List<SubscriptionUsageResponse.GiftUsageResponse> giftResponses =
                usage.gifts()
                        .stream()
                        .map(gift -> {

                            String userName =
                                    giftIdToUserName.getOrDefault(
                                            gift.giftId(),
                                            "Unknown"
                                    );

                            return new SubscriptionUsageResponse.GiftUsageResponse(
                                    gift.giftId(),
                                    userName,
                                    gift.limitGb(),
                                    gift.usedGb(),
                                    gift.remainGb(),
                                    gift.usagePercent()
                            );
                        })
                        .toList();

        return new SubscriptionUsageResponse(
                usage.subId(),
                now,
                subscription.getMember().getName(),
                familyRole,
                blocked,
                maskedPhone,
                // 요금제 이름
                subscription.getPlan().getPlanName(),
                // 개인 요금제
                usage.limitGb(),
                usage.usedGb(),
                usage.remainGb(),
                usage.usagePercent(),

                // gift 총합
                usage.giftTotalLimitGb(),
                usage.giftTotalUsedGb(),
                usage.giftTotalRemainGb(),
                usage.giftUsagePercent(),

                giftResponses
        );
    }
}
