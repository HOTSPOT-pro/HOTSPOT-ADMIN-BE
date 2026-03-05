package hotspot.admin.usage.subscriptionUsage.service.port;

import hotspot.admin.plan.domain.DataPeriod;
import hotspot.admin.usage.subscriptionUsage.domain.SubscriptionUsage;

import java.util.Map;

public interface SubscriptionUsageRepository {

    Map<Long, SubscriptionUsage> findSubscriptionUsages(
            Map<Long, DataPeriod> subPeriodMap
    );

    long findRemainingPlanKb(Long subId, DataPeriod dataPeriod);
}
