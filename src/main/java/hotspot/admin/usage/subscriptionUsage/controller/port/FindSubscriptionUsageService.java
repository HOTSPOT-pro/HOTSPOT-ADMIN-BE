package hotspot.admin.usage.subscriptionUsage.controller.port;

import java.util.List;

import hotspot.admin.usage.subscriptionUsage.controller.response.SubscriptionUsageResponse;

public interface FindSubscriptionUsageService {

    List<SubscriptionUsageResponse> findSubscriptionUsage(Long familyId);
}
