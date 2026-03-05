package hotspot.admin.outbox.consistencyOutbox.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
public class OutboxEvent {

    private final String id;

    private final String aggregateType;

    private final String aggregateId;

    private final String type;

    private final String payload;

    private final LocalDateTime timestamp;

    @Builder
    public OutboxEvent(String id, String aggregateType,
                       String aggregateId, String type,
                       String payload, LocalDateTime timestamp) {
        this.id = id;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.type = type;
        this.payload = payload;
        this.timestamp = timestamp;
    }

    public static OutboxEvent create(String aggregateType,
                                     String aggregateId,
                                     String type,
                                     String payload) {
        return OutboxEvent.builder()
                .id(UUID.randomUUID().toString())
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .type(type)
                .payload(payload)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
