package hotspot.admin.family.service.port;

import java.util.List;

import hotspot.admin.family.infrastructure.query.dto.FamilyControlMemberRow;
import hotspot.admin.family.infrastructure.query.dto.FamilyPolicyAppOptionRow;
import hotspot.admin.family.infrastructure.query.dto.FamilyPolicyAppPolicyRow;
import hotspot.admin.family.infrastructure.query.dto.FamilyPolicyMemberRow;
import hotspot.admin.family.infrastructure.query.dto.FamilyPolicyStatusRow;
import hotspot.admin.family.infrastructure.query.dto.FamilyPolicyTimeOptionRow;
import hotspot.admin.family.infrastructure.query.dto.FamilyPolicyTimePolicyRow;

public interface FamilySubQueryRepository {
    List<FamilyControlMemberRow> findFamilyControlMembers(Long familyId);

    List<FamilyPolicyMemberRow> findFamilyPolicyMembers(Long familyId);

    List<FamilyPolicyTimePolicyRow> findFamilyTimePolicies(Long familyId);

    List<FamilyPolicyTimeOptionRow> findFamilyTimePolicyOptions(Long familyId);

    List<FamilyPolicyAppPolicyRow> findFamilyAppPolicies(Long familyId);

    List<FamilyPolicyAppOptionRow> findFamilyAppPolicyOptions(Long familyId);

    List<FamilyPolicyStatusRow> findFamilyPolicyStatusRows(Long familyId);
}
