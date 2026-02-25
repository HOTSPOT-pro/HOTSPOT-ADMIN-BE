package hotspot.admin.family.controller.response;

import java.util.List;

import hotspot.admin.family.domain.PriorityType;
import lombok.Builder;

@Builder
public record FamilyControlStatusResponse(
        PriorityType priorityType,
        List<FamilyControlMemberItem> members
) {
}
