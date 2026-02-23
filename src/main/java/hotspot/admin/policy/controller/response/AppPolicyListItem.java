package hotspot.admin.policy.controller.response;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record AppPolicyListItem(
        Long policyId,
        String displayId,
        String policyName,
        String policyCode,
        Boolean active,
        LocalDateTime createdTime
) {
}
