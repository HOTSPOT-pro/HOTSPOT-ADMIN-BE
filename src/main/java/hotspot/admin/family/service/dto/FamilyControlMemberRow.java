package hotspot.admin.family.service.dto;

import hotspot.admin.family.domain.FamilyRole;
import lombok.Builder;

@Builder
public record FamilyControlMemberRow(
        Long subId,
        String memberName,
        FamilyRole familyRole,
        Boolean blocked,
        Long dataLimit,
        Integer priority
) {
}
