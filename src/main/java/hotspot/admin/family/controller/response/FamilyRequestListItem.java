package hotspot.admin.family.controller.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;

@Builder
public record FamilyRequestListItem(
        Long requestId,
        String requestDisplayId,
        Long familyId,
        String familyDisplayId,
        String familyName,
        Long requestSubId,
        String requesterName,
        String requesterPhoneNumber,
        List<FamilyRequestTargetItem> targets,
        String relationDocumentUrl,
        LocalDateTime requestedAt
) {
}
