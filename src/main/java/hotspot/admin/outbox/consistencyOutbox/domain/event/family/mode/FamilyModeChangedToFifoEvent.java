package hotspot.admin.outbox.consistencyOutbox.domain.event.family.mode;

import hotspot.admin.outbox.consistencyOutbox.domain.event.DomainEvent;

public record FamilyModeChangedToFifoEvent(
        String type,
        Long familyId,
        String mode, //FIFO
        String eventId
) implements DomainEvent {

    // "FAMILY_MODE_CHANGED"
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
