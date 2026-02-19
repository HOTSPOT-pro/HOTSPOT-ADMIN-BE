package hotspot.admin.family.controller.port;

import hotspot.admin.family.controller.request.FamilyListRequest;
import hotspot.admin.family.controller.response.FamilyListResponse;

public interface GetFamilyListService {
    FamilyListResponse getFamilyList(FamilyListRequest request);
}
