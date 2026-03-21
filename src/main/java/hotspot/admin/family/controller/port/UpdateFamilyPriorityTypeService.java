package hotspot.admin.family.controller.port;

import java.util.List;

import hotspot.admin.family.controller.request.MemberPriorityRequest;
import hotspot.admin.family.domain.PriorityType;

public interface UpdateFamilyPriorityTypeService {
    void updatePriorityType(Long familyId, PriorityType priorityType, List<MemberPriorityRequest> memberPriorities);
}
