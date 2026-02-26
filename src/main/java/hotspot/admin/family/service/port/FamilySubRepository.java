package hotspot.admin.family.service.port;

import hotspot.admin.family.domain.FamilyRole;

public interface FamilySubRepository {
    boolean existsFamilySub(Long familyId, Long subId);

    int findMaxPriority(Long familyId);

    void saveFamilySub(Long familyId, Long subId, FamilyRole familyRole, int priority, long dataLimit);

    int countActiveMembers(Long familyId);

    int updateDataLimit(Long familyId, long dataLimit);

    int updatePriority(Long familyId, int priority);
}
