package hotspot.admin.policy.controller.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import hotspot.admin.policy.domain.PolicyType;
import lombok.Builder;

@Builder
public record TimePolicyListItem(
        Long policyId,
        String displayId,
        String policyName,
        PolicyType policyType,
        String policyScheduleLabel,
        @JsonProperty("is_active") Boolean isActive,
        LocalDateTime createdTime
) {
}
