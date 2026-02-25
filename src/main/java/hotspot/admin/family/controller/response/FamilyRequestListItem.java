package hotspot.admin.family.controller.response;

import java.time.LocalDateTime;

import hotspot.admin.family.domain.FamilyRole;
import lombok.Builder;

@Builder
public record FamilyRequestListItem(
        Long requestId,
        String requestDisplayId,
        Long familyId,
        String familyDisplayId,
        String familyName,
        String requesterName,
        String requesterPhoneNumber,
        String targetName,
        String targetPhoneNumber,
        FamilyRole targetFamilyRole,
        String relationDocumentUrl,
        LocalDateTime requestedAt
) {
}
