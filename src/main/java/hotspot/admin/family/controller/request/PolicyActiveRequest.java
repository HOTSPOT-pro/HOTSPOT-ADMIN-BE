package hotspot.admin.family.controller.request;

import jakarta.validation.constraints.NotNull;

public record PolicyActiveRequest(
        @NotNull Long policyId,
        @NotNull Boolean isActive
) {
}
