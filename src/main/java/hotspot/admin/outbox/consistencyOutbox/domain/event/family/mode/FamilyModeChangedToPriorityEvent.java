package hotspot.admin.outbox.consistencyOutbox.domain.event.family.mode;

import java.util.List;

import hotspot.admin.outbox.consistencyOutbox.domain.event.DomainEvent;

public record FamilyModeChangedToPriorityEvent(
        String type,
        Long familyId,
        String mode, //PRIORITY
        List<Priority> priorities,
        String eventId
) implements DomainEvent {

    public record Priority(
            Long subId,
            Integer priority
    ) {}

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
