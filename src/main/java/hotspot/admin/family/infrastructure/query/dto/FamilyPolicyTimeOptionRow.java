package hotspot.admin.family.infrastructure.query.dto;

import hotspot.admin.policy.domain.PolicyType;
import lombok.Builder;

@Builder
public record FamilyPolicyTimeOptionRow(
        Long policyId,
        String policyName,
        String policyDescription,
        PolicyType policyType,
        String policySnapshotJson
) {
}
