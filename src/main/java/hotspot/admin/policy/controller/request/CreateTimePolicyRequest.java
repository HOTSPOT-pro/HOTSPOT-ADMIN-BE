package hotspot.admin.policy.controller.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import hotspot.admin.policy.controller.request.validation.ValidTimePolicyRequest;
import hotspot.admin.policy.domain.PolicySnapshot;
import hotspot.admin.policy.domain.PolicyType;

@ValidTimePolicyRequest
public record CreateTimePolicyRequest(
        @NotBlank String policyName,
        @NotBlank String policyDescription,
        @NotNull PolicyType policyType,
        @NotNull @Valid PolicySnapshot policySnapshot
) {
}
