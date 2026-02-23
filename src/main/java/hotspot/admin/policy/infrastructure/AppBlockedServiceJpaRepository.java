package hotspot.admin.policy.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import hotspot.admin.appservice.infrastructure.entity.AppBlockedServiceEntity;

public interface AppBlockedServiceJpaRepository extends JpaRepository<AppBlockedServiceEntity, Long> {
}
