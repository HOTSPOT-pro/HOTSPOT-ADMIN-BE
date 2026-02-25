package hotspot.admin.family.service.dto;

import lombok.Builder;

@Builder
public record FamilyPolicyTimePolicyRow(
        Long subId,
        String policyName
) {
}
