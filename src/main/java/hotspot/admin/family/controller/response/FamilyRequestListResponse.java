package hotspot.admin.family.controller.response;

import java.util.List;

import hotspot.admin.family.domain.ApplyType;
import hotspot.admin.family.domain.FamilyApplyStatus;
import lombok.Builder;

@Builder
public record FamilyRequestListResponse(
        Integer page,
        Integer size,
        Long totalElements,
        Integer totalPages,
        Boolean hasNext,
        ApplyType applyType,
        FamilyApplyStatus status,
        List<FamilyRequestListItem> requests
) {
}
