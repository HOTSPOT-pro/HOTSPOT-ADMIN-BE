package hotspot.admin.outbox.consistencyOutbox.domain.event.family.familyCreate;

import java.util.List;

import hotspot.admin.outbox.consistencyOutbox.domain.event.DomainEvent;

public record FamilyCreatedEvent(
        String type,
        Long familyId,
        List<Long> members,
        String eventId
) implements DomainEvent {

    @Override public String aggregateType() { return "family"; }
    @Override public String aggregateId() { return familyId.toString(); }
}