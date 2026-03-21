package hotspot.admin.family.service.dto;

import java.util.List;

import hotspot.admin.family.domain.FamilyApply;

public record FamilyRequestOutboxInfo(
        FamilyApply familyApply,
        List<String> targetNames
) {
}
