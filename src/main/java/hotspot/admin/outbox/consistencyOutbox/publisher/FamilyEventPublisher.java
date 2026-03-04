package hotspot.admin.outbox.consistencyOutbox.publisher;

import java.util.List;
import java.util.UUID;

import hotspot.admin.outbox.consistencyOutbox.domain.event.family.familyCreate.FamilyCreatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import hotspot.admin.outbox.consistencyOutbox.domain.event.family.familyMember.FamilyMemberAddedEvent;
import hotspot.admin.outbox.consistencyOutbox.domain.event.family.familyMember.FamilyMemberRemovedEvent;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FamilyEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publishFamilyCreated(Long familyId, List<Long> members) {
        FamilyCreatedEvent event = new FamilyCreatedEvent(
                "FAMILY_CREATE",
                familyId,
                members,
                UUID.randomUUID().toString()
        );
        eventPublisher.publishEvent(event);
    }

    public void publishMemberAdded(Long familyId, Long subId) {

        FamilyMemberAddedEvent event =
                new FamilyMemberAddedEvent(
                        "FAMILY_MEMBER_ADDED",
                        familyId,
                        subId,
                        UUID.randomUUID().toString()
                );

        eventPublisher.publishEvent(event);
    }

    public void publishMemberRemoved(Long familyId, Long subId) {

        FamilyMemberRemovedEvent event =
                new FamilyMemberRemovedEvent(
                        "FAMILY_MEMBER_REMOVED",
                        familyId,
                        subId,
                        UUID.randomUUID().toString()
                );

        eventPublisher.publishEvent(event);
    }
}