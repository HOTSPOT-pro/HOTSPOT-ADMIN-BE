package hotspot.admin.family.controller.response;

import java.util.List;

import lombok.Builder;

@Builder
public record FamilyListResponse(
        Integer page,
        Integer size,
        Long totalElements,
        Integer totalPages,
        Boolean hasNext,
        List<FamilyListItem> familyList
) {
}
