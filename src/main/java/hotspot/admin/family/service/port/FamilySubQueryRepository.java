package hotspot.admin.family.service.port;

import java.util.List;

import hotspot.admin.family.service.dto.FamilyControlMemberRow;
import hotspot.admin.family.service.dto.FamilyPolicyAppPolicyRow;
import hotspot.admin.family.service.dto.FamilyPolicyMemberRow;
import hotspot.admin.family.service.dto.FamilyPolicyStatusRow;
import hotspot.admin.family.service.dto.FamilyPolicyTimePolicyRow;

public interface FamilySubQueryRepository {
    List<FamilyControlMemberRow> findFamilyControlMembers(Long familyId);

    List<FamilyPolicyMemberRow> findFamilyPolicyMembers(Long familyId);

    List<FamilyPolicyTimePolicyRow> findFamilyTimePolicies(Long familyId);

    List<FamilyPolicyAppPolicyRow> findFamilyAppPolicies(Long familyId);

    List<FamilyPolicyStatusRow> findFamilyPolicyStatusRows(Long familyId);
}
