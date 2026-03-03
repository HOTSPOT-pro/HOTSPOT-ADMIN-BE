package hotspot.admin.family.infrastructure.query.dto;

import java.time.LocalDateTime;

import hotspot.admin.family.domain.FamilyRole;
import lombok.Builder;

@Builder
public record FamilyRequestListRow(
        Long requestId,
        Long familyId,
        Long requestSubId,
        String requesterName,
        String requesterPhoneNumberEnc,
        Long targetSubId,
        String targetName,
        String targetPhoneNumberEnc,
        FamilyRole targetFamilyRole,
        String relationDocumentUrl,
        LocalDateTime requestedAt
) {
}
