package hotspot.admin.outbox.consistencyOutbox.infrastructure.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import hotspot.admin.outbox.consistencyOutbox.domain.OutboxEvent;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "outbox_event")
public class OutboxEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "aggregatetype", nullable = false)
    private String aggregateType;

    @Column(name = "aggregateid", nullable = false)
    private String aggregateId;

    @Column(nullable = false)
    private String type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private String payload;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public static OutboxEventEntity domainToEntity(OutboxEvent outboxEvent) {
        return OutboxEventEntity.builder()
                .id(outboxEvent.getId())
                .aggregateType(outboxEvent.getAggregateType())
                .aggregateId(outboxEvent.getAggregateId())
                .type(outboxEvent.getType())
                .payload(outboxEvent.getPayload())
                .timestamp(outboxEvent.getTimestamp())
                .build();
    }

    public OutboxEvent entityToDomain() {
        return OutboxEvent.builder()
                .id(id)
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .type(type)
                .payload(payload)
                .timestamp(timestamp)
                .build();
    }
}
