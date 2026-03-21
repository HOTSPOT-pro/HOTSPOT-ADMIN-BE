package hotspot.admin.policy.controller.port;

import hotspot.admin.policy.controller.response.UpdatePolicyActiveResponse;
import hotspot.admin.policy.domain.AdminPolicyType;

public interface UpdatePolicyActiveService {

    UpdatePolicyActiveResponse updatePolicyActive(AdminPolicyType policyType, Long policyId, Boolean isActive);
}
