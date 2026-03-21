package hotspot.admin.family.infrastructure.query.dto;

import hotspot.admin.family.domain.FamilyRole;
import lombok.Builder;

@Builder
public record FamilyPolicyMemberRow(
        Long subId,
        String memberName,
        String phoneNumberEnc,
        FamilyRole familyRole,
        Boolean blocked
) {
}
