package hotspot.admin.outbox.consistencyOutbox.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import hotspot.admin.outbox.consistencyOutbox.infrastructure.entity.OutboxEventEntity;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventEntity, Long> {
}
