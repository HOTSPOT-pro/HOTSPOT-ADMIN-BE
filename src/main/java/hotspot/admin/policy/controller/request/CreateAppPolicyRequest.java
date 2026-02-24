package hotspot.admin.policy.controller.request;

import jakarta.validation.constraints.NotBlank;

public record CreateAppPolicyRequest(
        @NotBlank String policyName,
        @NotBlank String policyCode
) {
}
