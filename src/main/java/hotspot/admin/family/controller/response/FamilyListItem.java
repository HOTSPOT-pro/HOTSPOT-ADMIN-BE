package hotspot.admin.family.controller.response;

import lombok.Builder;

@Builder
public record FamilyListItem(
        Long familyId,
        String displayId,
        String representativeName,
        String phoneNumber,
        Integer memberCount,
        Long usedData,
        Long remainingData
) {
}
