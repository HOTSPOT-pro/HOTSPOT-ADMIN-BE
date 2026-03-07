package hotspot.admin.outbox.consistencyOutbox.util;

import hotspot.admin.outbox.consistencyOutbox.domain.event.subscription.policyBlock.PolicyBlockSnapshotEvent;
import hotspot.admin.policy.domain.BlockPolicy;
import hotspot.admin.policy.domain.PolicyDay;
import hotspot.admin.policy.domain.PolicyPayload;
import hotspot.admin.policy.domain.PolicySnapshot;
import hotspot.admin.policy.domain.PolicyType;
import hotspot.admin.policy.service.port.BlockPolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PolicyBlockSnapshotPublisherTest {

    private BlockPolicyRepository blockPolicyRepository;
    private ApplicationEventPublisher eventPublisher;
    private PolicyBlockSnapshotPublisher publisher;

    @BeforeEach
    void setUp() {
        blockPolicyRepository = mock(BlockPolicyRepository.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        publisher = new PolicyBlockSnapshotPublisher(blockPolicyRepository, eventPublisher);
    }

    @Test
    @DisplayName("활성 정책이 없으면 빈 payload 목록으로 이벤트를 발행한다")
    void publishEmptyPolicies() {
        publisher.publish(10L, List.of());

        ArgumentCaptor<PolicyBlockSnapshotEvent> captor =
                ArgumentCaptor.forClass(PolicyBlockSnapshotEvent.class);

        verify(eventPublisher).publishEvent(captor.capture());

        PolicyBlockSnapshotEvent event = captor.getValue();
        assertThat(event.subId()).isEqualTo(10L);
        assertThat(event.policies()).isEmpty();
        assertThat(event.type()).isEqualTo("POLICY_SNAPSHOT");
    }

    @Test
    @DisplayName("활성 정책이 있으면 snapshot을 payload로 변환해 이벤트를 발행한다")
    void publishActivePolicies() {
        PolicySnapshot snapshot = PolicySnapshot.builder()
                .days(List.of(PolicyDay.MON, PolicyDay.WED))
                .startTime("06:00")
                .endTime("07:00")
                .build();

        BlockPolicy policy = BlockPolicy.builder()
                .blockPolicyId(1L)
                .policyType(PolicyType.SCHEDULED)
                .policySnapshot(snapshot)
                .build();

        when(blockPolicyRepository.findAllById(List.of(1L)))
                .thenReturn(List.of(policy));

        publisher.publish(10L, List.of(1L));

        ArgumentCaptor<PolicyBlockSnapshotEvent> captor =
                ArgumentCaptor.forClass(PolicyBlockSnapshotEvent.class);

        verify(eventPublisher).publishEvent(captor.capture());

        PolicyBlockSnapshotEvent event = captor.getValue();
        assertThat(event.subId()).isEqualTo(10L);
        assertThat(event.policies()).hasSize(1);

        PolicyPayload payload = event.policies().get(0);
        assertThat(payload.policyId()).isEqualTo(1L);
        assertThat(payload.encoded()).isEqualTo("1,3|06:00|07:00");
    }
}
