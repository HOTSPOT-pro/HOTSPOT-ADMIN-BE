package hotspot.admin.usage.familyUsage.service.port;


import java.util.List;

import hotspot.admin.usage.familyUsage.domain.FamilyUsage;
import hotspot.admin.usage.familyUsage.service.schema.FamilySubList;

public interface FamilyUsageRepository {

    FamilyUsage findFamilyUsage(Long familyId, List<FamilySubList> familySubList);
}
