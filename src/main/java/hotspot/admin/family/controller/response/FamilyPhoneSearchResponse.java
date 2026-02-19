package hotspot.admin.family.controller.response;

import lombok.Builder;

@Builder
public record FamilyPhoneSearchResponse(
        FamilyListItem family
) {
}
