package hotspot.admin.policy.controller.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder
public record AppPolicyListItem(
        Long policyId,
        String displayId,
        String policyName,
        String policyCode,
        @JsonProperty("is_active") Boolean isActive,
        LocalDateTime createdTime
) {
}
