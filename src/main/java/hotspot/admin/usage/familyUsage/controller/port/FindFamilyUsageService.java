package hotspot.admin.usage.familyUsage.controller.port;

import hotspot.admin.usage.familyUsage.controller.response.FamilyUsageResponse;

public interface FindFamilyUsageService {

    FamilyUsageResponse findFamilyUsage(Long familyId);
}
