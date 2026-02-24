package hotspot.admin.policy.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder
public record UpdatePolicyActiveResponse(
        Long policyId,
        String displayId,
        @JsonProperty("is_active") Boolean isActive
) {
}
