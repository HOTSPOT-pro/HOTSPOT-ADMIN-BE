package hotspot.admin.family.controller.response;

import lombok.Builder;

@Builder
public record FamilyPolicyAppItem(
        Long policyId,
        String policyName,
        Boolean isActive
) {
}
