package hotspot.admin.family.controller.port;

import hotspot.admin.family.controller.response.FamilySummaryResponse;

public interface GetFamilySummaryService {
    FamilySummaryResponse getFamilySummary(Long familyId);
}
