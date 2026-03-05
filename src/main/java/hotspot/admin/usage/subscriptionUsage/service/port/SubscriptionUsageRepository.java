package hotspot.admin.usage.subscriptionUsage.service.port;

import java.util.Map;

import hotspot.admin.plan.domain.DataPeriod;
import hotspot.admin.usage.subscriptionUsage.domain.SubscriptionUsage;

public interface SubscriptionUsageRepository {

    Map<Long, SubscriptionUsage> findSubscriptionUsages(
            Map<Long, DataPeriod> subPeriodMap
    );
}
