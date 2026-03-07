package hotspot.admin.subscription.infrastructure;

import hotspot.admin.subscription.service.port.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SubscriptionRepositoryImpl implements SubscriptionRepository {

    private final SubscriptionJpaRepository subscriptionJpaRepository;

    @Override
    public boolean findIsLockedBySubId(Long subId) {
        return subscriptionJpaRepository.findIsLockedBySubId(subId);
    }
}
