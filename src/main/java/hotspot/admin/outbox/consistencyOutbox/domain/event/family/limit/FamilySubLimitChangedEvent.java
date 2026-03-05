package hotspot.admin.outbox.consistencyOutbox.domain.event.family.limit;

import hotspot.admin.outbox.consistencyOutbox.domain.event.DomainEvent;

public record FamilySubLimitChangedEvent(
        String type,
        Long familyId,
        Long subId,
        Long newLimit,
        String eventId
) implements DomainEvent {

    //FAMILY_SUB_LIMIT_CHANGED
    @Override
    public String type() {
        return type;
    }

    @Override
    public String aggregateType() {
        return "family";
    }

    @Override
    public String aggregateId() {
        return familyId.toString();
    }
}
