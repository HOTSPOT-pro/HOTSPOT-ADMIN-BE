package hotspot.admin.family.controller.response;

import java.util.List;

import lombok.Builder;

@Builder
public record FamilyListResponse(
        Integer size,
        Long nextCursor,
        Boolean hasNext,
        List<FamilyListItem> familyList
) {
}
