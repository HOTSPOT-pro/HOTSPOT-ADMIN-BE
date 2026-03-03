package hotspot.admin.family.infrastructure.query.dto;

import lombok.Builder;

@Builder
public record FamilyPolicyAppPolicyRow(
        Long subId,
        String blockedServiceName
) {
}
