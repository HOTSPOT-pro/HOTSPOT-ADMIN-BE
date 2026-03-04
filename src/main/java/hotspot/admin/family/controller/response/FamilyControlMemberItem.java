package hotspot.admin.family.controller.response;

import hotspot.admin.family.domain.FamilyRole;
import lombok.Builder;

@Builder
public record FamilyControlMemberItem(
        Long subId,
        String memberName,
        FamilyRole familyRole,
        Boolean isBlocked,
        Double dataLimitGb,
        Integer priorityOrder
) {
}
