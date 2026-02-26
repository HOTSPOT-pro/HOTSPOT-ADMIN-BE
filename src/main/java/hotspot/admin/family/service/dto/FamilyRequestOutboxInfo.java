package hotspot.admin.family.service.dto;

import hotspot.admin.family.domain.FamilyApply;

public record FamilyRequestOutboxInfo(
        FamilyApply familyApply,
        String targetName
) {
}
