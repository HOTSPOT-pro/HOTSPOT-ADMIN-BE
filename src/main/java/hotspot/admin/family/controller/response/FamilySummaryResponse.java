package hotspot.admin.family.controller.response;

import lombok.Builder;

@Builder
public record FamilySummaryResponse(
        Long familyId,
        String displayId,
        String representativeName,
        String phoneNumber,
        Integer memberCount
) {
}
