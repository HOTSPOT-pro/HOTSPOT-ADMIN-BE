package hotspot.admin.outbox.consistencyOutbox.util;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import hotspot.admin.outbox.consistencyOutbox.domain.event.subscription.policyBlock.PolicyBlockSnapshotEvent;
import hotspot.admin.policy.domain.BlockPolicy;
import hotspot.admin.policy.domain.PolicyPayload;
import hotspot.admin.policy.service.port.BlockPolicyRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PolicyBlockSnapshotPublisher {

    private final BlockPolicyRepository blockPolicyRepository;
    private final ApplicationEventPublisher eventPublisher;

    public void publish(Long subId, List<Long> activatePolicyIds) {

        if (activatePolicyIds.isEmpty()) {

            eventPublisher.publishEvent(
                    new PolicyBlockSnapshotEvent(
                            "POLICY_SNAPSHOT",
                            subId,
                            List.of(),
                            UUID.randomUUID().toString()
                    )
            );

            return;
        }

        Map<Long, BlockPolicy> policyMap =
                blockPolicyRepository.findAllById(activatePolicyIds)
                        .stream()
                        .collect(Collectors.toMap(
                                BlockPolicy::getBlockPolicyId,
                                policy -> policy
                        ));

        List<PolicyPayload> payloads =
                activatePolicyIds.stream()
                        .map(policyId -> {

                            BlockPolicy policy =
                                    policyMap.get(policyId);

                            return PolicySnapshotUtil.map(
                                    policy.getBlockPolicyId(),
                                    policy.getPolicyType(),
                                    policy.getPolicySnapshot()
                            );
                        })
                        .toList();

        eventPublisher.publishEvent(
                new PolicyBlockSnapshotEvent(
                        "POLICY_SNAPSHOT",
                        subId,
                        payloads,
                        UUID.randomUUID().toString()
                )
        );
    }
}
