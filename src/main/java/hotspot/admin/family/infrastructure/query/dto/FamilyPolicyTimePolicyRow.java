package hotspot.admin.family.infrastructure.query.dto;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record FamilyPolicyTimePolicyRow(
        Long policySubId,
        Long subId,
        Long policyId,
        String policyName,
        LocalDateTime modifiedTime
) {
}
