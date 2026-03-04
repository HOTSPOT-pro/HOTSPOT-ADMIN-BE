package hotspot.admin.outbox.consistencyOutbox.domain.event.family.familyMember;

import hotspot.admin.outbox.consistencyOutbox.domain.event.DomainEvent;

public record FamilyMemberAddedEvent(
        String type,
        Long familyId,
        Long subId,
        String eventId
) implements DomainEvent {

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
