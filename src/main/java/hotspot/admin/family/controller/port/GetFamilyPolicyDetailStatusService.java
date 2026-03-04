package hotspot.admin.family.controller.port;

import hotspot.admin.family.controller.response.FamilyPolicyMemberDetailItem;

public interface GetFamilyPolicyDetailStatusService {
    FamilyPolicyMemberDetailItem getFamilyPolicyDetailStatus(Long familyId, Long subId);
}
