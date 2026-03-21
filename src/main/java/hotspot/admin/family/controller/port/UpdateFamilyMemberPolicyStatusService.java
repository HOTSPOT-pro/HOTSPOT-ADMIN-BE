package hotspot.admin.family.controller.port;

import java.util.List;

import hotspot.admin.family.controller.request.PolicyActiveRequest;

public interface UpdateFamilyMemberPolicyStatusService {
    void updateMemberTimePolicyStatus(
            Long familyId,
            Long subId,
            List<PolicyActiveRequest> policies
    );

    void updateMemberAppPolicyStatus(
            Long familyId,
            Long subId,
            List<PolicyActiveRequest> policies
    );
}
