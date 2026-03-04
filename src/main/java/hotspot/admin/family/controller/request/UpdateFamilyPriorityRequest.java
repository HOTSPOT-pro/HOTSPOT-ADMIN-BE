package hotspot.admin.family.controller.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import hotspot.admin.family.domain.PriorityType;

public record UpdateFamilyPriorityRequest(
        @NotNull PriorityType priorityType,
        @Valid List<MemberPriorityRequest> memberPriorities
) {
}
