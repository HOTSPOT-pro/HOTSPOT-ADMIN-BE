package hotspot.admin.family.controller.port;

import hotspot.admin.family.controller.response.FamilyControlStatusResponse;

public interface GetFamilyControlStatusService {
    FamilyControlStatusResponse getFamilyControlStatus(Long familyId);
}
