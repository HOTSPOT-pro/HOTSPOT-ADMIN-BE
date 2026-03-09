package hotspot.admin.family.service.port;

import hotspot.admin.family.infrastructure.schema.FamilyDataControl;

public interface FamilyDataLimitRepository {

    FamilyDataControl findFamilyDataLimit(Long familyId);
}
