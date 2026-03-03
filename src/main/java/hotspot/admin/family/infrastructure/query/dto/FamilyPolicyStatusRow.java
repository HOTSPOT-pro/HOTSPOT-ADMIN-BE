package hotspot.admin.family.infrastructure.query.dto;

import java.util.List;

import hotspot.admin.family.domain.FamilyRole;
import lombok.Builder;

@Builder
public record FamilyPolicyStatusRow(
        Long subId,
        String memberName,
        String phoneNumberEnc,
        FamilyRole familyRole,
        Boolean blocked,
        List<String> appliedTimePolicies,
        List<String> appliedBlockedServicePolicies
) {
}
