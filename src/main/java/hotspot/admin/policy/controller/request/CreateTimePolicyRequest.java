package hotspot.admin.policy.controller.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import hotspot.admin.policy.domain.PolicySnapshot;
import hotspot.admin.policy.domain.PolicyType;

public record CreateTimePolicyRequest(
        @NotBlank String policyName,
        @NotNull PolicyType policyType,
        @NotNull @Valid PolicySnapshot policySnapshot
) {
}
