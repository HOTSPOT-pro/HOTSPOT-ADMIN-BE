package hotspot.admin.family.infrastructure.query.dto;

import hotspot.admin.family.domain.FamilyRole;
import lombok.Builder;

@Builder
public record FamilyApprovalTargetInfo(
        Long familyId,
        Long targetSubId,
        FamilyRole targetFamilyRole
) {
}
