package hotspot.admin.outbox.consistencyOutbox.domain.event.subscription.lock;

import hotspot.admin.outbox.consistencyOutbox.domain.event.DomainEvent;

public record SubscriptionUnlockedEvent(
        String type,
        Long subId,
        String eventId
) implements DomainEvent {

    @Override
    public String type() {
        return type;
    }

    @Override
    public String aggregateType() {
        return "subscription";
    }

    @Override
    public String aggregateId() {
        return subId.toString();
    }
}
