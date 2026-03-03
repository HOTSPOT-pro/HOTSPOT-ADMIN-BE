package hotspot.admin.family.infrastructure.query.dto;

import lombok.Builder;

@Builder
public record FamilyPolicyTimePolicyRow(
        Long subId,
        String policyName
) {
}
