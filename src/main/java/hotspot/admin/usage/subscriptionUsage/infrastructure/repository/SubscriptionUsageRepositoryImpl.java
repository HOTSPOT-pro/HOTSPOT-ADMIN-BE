package hotspot.admin.usage.subscriptionUsage.infrastructure.repository;

import hotspot.admin.plan.domain.DataPeriod;
import hotspot.admin.usage.subscriptionUsage.domain.SubscriptionUsage;
import hotspot.admin.usage.subscriptionUsage.service.port.SubscriptionUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
@RequiredArgsConstructor
public class SubscriptionUsageRepositoryImpl
        implements SubscriptionUsageRepository {

    private final SubscriptionUsageRedisRepository redisRepository;

    @Override
    public Map<Long, SubscriptionUsage> findSubscriptionUsages(
            Map<Long, DataPeriod> subPeriodMap
    ) {
        return redisRepository.findSubscriptionUsages(subPeriodMap);
    }

    @Override
    public long findRemainingPlanKb(
            Long subId,
            DataPeriod dataPeriod
    ) {
        return redisRepository.findRemainingPlanKb(subId, dataPeriod);
    }
}
