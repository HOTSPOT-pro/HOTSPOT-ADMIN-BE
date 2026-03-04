package hotspot.admin.outbox.consistencyOutbox.publisher;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import hotspot.admin.outbox.consistencyOutbox.domain.event.family.familyMember.FamilyMemberAddedEvent;
import hotspot.admin.outbox.consistencyOutbox.domain.event.family.familyMember.FamilyMemberRemovedEvent;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FamilyEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

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