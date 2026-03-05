package hotspot.admin.family.controller.response;

import java.util.List;

import hotspot.admin.family.domain.FamilyRole;
import lombok.Builder;

@Builder
public record FamilyMemberTimePolicyStatusResponse(
        String memberName,
        String phoneNumber,
        FamilyRole familyRole,
        Boolean blocked,
        List<FamilyPolicyTimeItem> appliedTimePolicies
) {
}
