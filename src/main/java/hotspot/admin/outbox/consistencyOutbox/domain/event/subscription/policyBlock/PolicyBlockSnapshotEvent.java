package hotspot.admin.outbox.consistencyOutbox.domain.event.subscription.policyBlock;

import java.util.List;

import hotspot.admin.outbox.consistencyOutbox.domain.event.DomainEvent;
import hotspot.admin.policy.domain.PolicyPayload;

public record PolicyBlockSnapshotEvent(
        String type,
        Long subId,
        List<PolicyPayload> policies,
        String eventId
) implements DomainEvent {

    @Override
    public String aggregateType() {
        return "subscription";
    }

    @Override
    public String aggregateId() {
        return subId.toString();
    }
}
