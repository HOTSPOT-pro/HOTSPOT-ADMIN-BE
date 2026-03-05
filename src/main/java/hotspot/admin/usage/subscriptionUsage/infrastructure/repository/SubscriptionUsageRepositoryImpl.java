package hotspot.admin.usage.subscriptionUsage.infrastructure.repository;

import java.util.Map;

import org.springframework.stereotype.Repository;

import hotspot.admin.plan.domain.DataPeriod;
import hotspot.admin.usage.subscriptionUsage.domain.SubscriptionUsage;
import hotspot.admin.usage.subscriptionUsage.service.port.SubscriptionUsageRepository;
import lombok.RequiredArgsConstructor;

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
}
