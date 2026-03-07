package hotspot.admin.outbox.consistencyOutbox.publisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import hotspot.admin.outbox.consistencyOutbox.domain.event.family.familyCreate.FamilyCreatedEvent;
import hotspot.admin.outbox.consistencyOutbox.domain.event.family.familyMember.FamilyMemberAddedEvent;
import hotspot.admin.outbox.consistencyOutbox.domain.event.family.familyMember.FamilyMemberRemovedEvent;
import hotspot.admin.outbox.consistencyOutbox.publisher.family.FamilyEventPublisher;

class FamilyEventPublisherTest {

    private ApplicationEventPublisher eventPublisher;
    private FamilyEventPublisher publisher;

    @BeforeEach
    void setUp() {
        eventPublisher = mock(ApplicationEventPublisher.class);
        publisher = new FamilyEventPublisher(eventPublisher);
    }

    @Test
    @DisplayName("FamilyCreatedEvent 발행")
    void publishFamilyCreated() {

        Long familyId = 1L;
        List<Long> members = List.of(100L, 101L);

        publisher.publishFamilyCreated(familyId, members);

        ArgumentCaptor<FamilyCreatedEvent> captor =
                ArgumentCaptor.forClass(FamilyCreatedEvent.class);

        verify(eventPublisher).publishEvent(captor.capture());

        FamilyCreatedEvent event = captor.getValue();

        assertThat(event.familyId()).isEqualTo(familyId);
        assertThat(event.members()).isEqualTo(members);
        assertThat(event.type()).isEqualTo("FAMILY_CREATE");
        assertThat(event.eventId()).isNotNull();
    }

    @Test
    @DisplayName("FamilyMemberAddedEvent 발행")
    void publishMemberAdded() {

        Long familyId = 2L;
        Long subId = 200L;

        publisher.publishMemberAdded(familyId, subId);

        ArgumentCaptor<FamilyMemberAddedEvent> captor =
                ArgumentCaptor.forClass(FamilyMemberAddedEvent.class);

        verify(eventPublisher).publishEvent(captor.capture());

        FamilyMemberAddedEvent event = captor.getValue();

        assertThat(event.familyId()).isEqualTo(familyId);
        assertThat(event.subId()).isEqualTo(subId);
        assertThat(event.type()).isEqualTo("FAMILY_MEMBER_ADDED");
        assertThat(event.eventId()).isNotNull();
    }

    @Test
    @DisplayName("FamilyMemberRemovedEvent 발행")
    void publishMemberRemoved() {

        Long familyId = 3L;
        Long subId = 300L;

        publisher.publishMemberRemoved(familyId, subId);

        ArgumentCaptor<FamilyMemberRemovedEvent> captor =
                ArgumentCaptor.forClass(FamilyMemberRemovedEvent.class);

        verify(eventPublisher).publishEvent(captor.capture());

        FamilyMemberRemovedEvent event = captor.getValue();

        assertThat(event.familyId()).isEqualTo(familyId);
        assertThat(event.subId()).isEqualTo(subId);
        assertThat(event.type()).isEqualTo("FAMILY_MEMBER_REMOVED");
        assertThat(event.eventId()).isNotNull();
    }
}
