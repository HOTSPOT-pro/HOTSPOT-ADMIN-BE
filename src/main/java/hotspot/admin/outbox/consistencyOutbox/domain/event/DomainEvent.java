package hotspot.admin.outbox.consistencyOutbox.domain.event;

public interface DomainEvent {

    String type();
    String aggregateType();
    String aggregateId();
}
