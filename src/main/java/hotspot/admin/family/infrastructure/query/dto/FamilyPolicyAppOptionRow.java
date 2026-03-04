package hotspot.admin.family.infrastructure.query.dto;

import lombok.Builder;

@Builder
public record FamilyPolicyAppOptionRow(
        Long policyId,
        String policyName
) {
}
