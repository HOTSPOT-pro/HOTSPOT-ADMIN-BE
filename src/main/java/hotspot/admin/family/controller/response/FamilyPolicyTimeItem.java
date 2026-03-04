package hotspot.admin.family.controller.response;

import hotspot.admin.policy.domain.PolicyType;
import lombok.Builder;

@Builder
public record FamilyPolicyTimeItem(
        Long policyId,
        String policyName,
        String policyDescription,
        PolicyType policyType,
        String policyScheduleLabel,
        Boolean isActive
) {
}
