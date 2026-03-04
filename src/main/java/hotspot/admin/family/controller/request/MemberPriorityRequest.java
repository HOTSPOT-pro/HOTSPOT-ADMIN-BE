package hotspot.admin.family.controller.request;

import jakarta.validation.constraints.NotNull;

public record MemberPriorityRequest(
        @NotNull Long subId,
        @NotNull Integer priority
) {
}
