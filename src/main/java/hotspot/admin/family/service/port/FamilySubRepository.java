package hotspot.admin.family.service.port;

import java.util.List;
import java.util.Optional;

import hotspot.admin.family.domain.FamilyRole;
import hotspot.admin.family.domain.FamilySub;

public interface FamilySubRepository {

    List<FamilySub> findByFamilyId(Long familyId);

    boolean existsFamilySub(Long familyId, Long subId);

    int findMaxPriority(Long familyId);

    void saveFamilySub(Long familyId, Long subId, FamilyRole familyRole, int priority, long dataLimit);

    int countActiveMembers(Long familyId);

    int updateDataLimit(Long familyId, long dataLimit);

    int updatePriority(Long familyId, int priority);

    int updateMemberPriority(Long familyId, Long subId, int priority);

    int updateMemberDataLimit(Long familyId, Long subId, long dataLimit);

    int updateMemberBlocked(Long familyId, Long subId, boolean isBlocked);

    Optional<FamilyRole> findFamilyRole(Long familyId, Long subId);

    int updateMemberFamilyRole(Long familyId, Long subId, FamilyRole familyRole);
}
