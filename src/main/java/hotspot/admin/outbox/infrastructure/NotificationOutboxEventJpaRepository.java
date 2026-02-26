package hotspot.admin.outbox.infrastructure;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import hotspot.admin.outbox.infrastructure.entity.NotificationOutboxEventEntity;

public interface NotificationOutboxEventJpaRepository extends JpaRepository<NotificationOutboxEventEntity, UUID> {
}
