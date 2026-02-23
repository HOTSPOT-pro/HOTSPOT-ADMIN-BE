package hotspot.admin.policy.controller.request;

import jakarta.validation.constraints.NotNull;

public record UpdatePolicyActiveRequest(
        @NotNull Boolean isActive
) {
}
