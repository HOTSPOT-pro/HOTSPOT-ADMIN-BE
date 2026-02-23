package hotspot.admin.policy.controller.response;

import java.time.LocalDateTime;

import hotspot.admin.policy.domain.PolicyType;
import lombok.Builder;

@Builder
public record TimePolicyListItem(
        Long policyId,
        String displayId,
        String policyName,
        PolicyType policyType,
        String policyScheduleLabel,
        Boolean active,
        LocalDateTime createdTime
) {
}
