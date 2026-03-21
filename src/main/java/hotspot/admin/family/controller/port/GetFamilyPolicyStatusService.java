package hotspot.admin.family.controller.port;

import java.util.List;

import hotspot.admin.family.controller.response.FamilyPolicyMemberStatusItem;

public interface GetFamilyPolicyStatusService {
    List<FamilyPolicyMemberStatusItem> getFamilyPolicyStatus(Long familyId);
}
