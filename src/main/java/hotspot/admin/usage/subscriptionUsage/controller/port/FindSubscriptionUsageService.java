package hotspot.admin.usage.subscriptionUsage.controller.port;

import hotspot.admin.usage.subscriptionUsage.controller.response.SubscriptionUsageResponse;

import java.util.List;

public interface FindSubscriptionUsageService {

    List<SubscriptionUsageResponse> findSubscriptionUsage(Long subscriptionId);
}
