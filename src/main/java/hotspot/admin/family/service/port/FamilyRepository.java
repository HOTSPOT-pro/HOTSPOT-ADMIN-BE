package hotspot.admin.family.service.port;

import java.util.Optional;

import hotspot.admin.family.domain.PriorityType;

public interface FamilyRepository {
    boolean existsFamilyById(Long familyId);

    Optional<PriorityType> findFamilyPriorityType(Long familyId);

    int updateFamilySummary(Long familyId, int familyNum, long familyDataAmount);
}
