package hotspot.admin.family.controller.port;

import hotspot.admin.family.controller.request.FamilyRequestListRequest;
import hotspot.admin.family.controller.response.FamilyRequestListResponse;
import hotspot.admin.family.domain.ApplyType;
import hotspot.admin.family.domain.FamilyApplyStatus;

public interface GetFamilyRequestListService {
    FamilyRequestListResponse getFamilyRequests(
            ApplyType applyType,
            FamilyApplyStatus status,
            FamilyRequestListRequest request
    );
}
