package hotspot.admin.family.service.dto;

import hotspot.admin.family.domain.FamilyRole;
import lombok.Builder;

@Builder
public record FamilyAddApprovalInfo(
        Long familyId,
        Long targetSubId,
        FamilyRole targetFamilyRole
) {
}
