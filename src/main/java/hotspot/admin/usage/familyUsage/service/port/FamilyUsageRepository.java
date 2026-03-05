package hotspot.admin.usage.familyUsage.service.port;


import hotspot.admin.usage.familyUsage.domain.FamilyUsage;
import hotspot.admin.usage.familyUsage.service.schema.FamilySubList;

import java.util.List;

public interface FamilyUsageRepository {

    FamilyUsage findFamilyUsage(Long familyId, List<FamilySubList> familySubList);
}
