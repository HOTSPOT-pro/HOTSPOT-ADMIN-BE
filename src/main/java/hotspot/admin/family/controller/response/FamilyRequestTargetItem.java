package hotspot.admin.family.controller.response;

import hotspot.admin.family.domain.FamilyRole;
import lombok.Builder;

@Builder
public record FamilyRequestTargetItem(
        Long targetSubId,
        String targetName,
        String targetPhoneNumber,
        FamilyRole targetFamilyRole
) {
}
