package hotspot.admin.family.service.dto;

import lombok.Builder;

@Builder
public record FamilyPolicyAppPolicyRow(
        Long subId,
        String blockedServiceName
) {
}
