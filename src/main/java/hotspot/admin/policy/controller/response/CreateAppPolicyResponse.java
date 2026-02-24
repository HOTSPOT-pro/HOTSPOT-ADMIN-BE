package hotspot.admin.policy.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record CreateAppPolicyResponse(
        Long policyId,
        String displayId,
        @JsonProperty("is_active") Boolean isActive
) {
}
