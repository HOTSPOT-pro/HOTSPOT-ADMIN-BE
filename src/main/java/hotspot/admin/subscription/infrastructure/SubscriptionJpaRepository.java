package hotspot.admin.subscription.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import hotspot.admin.subscription.infrastructure.entity.SubscriptionEntity;

public interface SubscriptionJpaRepository extends JpaRepository<SubscriptionEntity, Long> {
}
