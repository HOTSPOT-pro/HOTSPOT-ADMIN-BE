package hotspot.admin.policy.controller.port;

import hotspot.admin.policy.domain.AdminPolicyType;

public interface DeletePolicyService {

    void deletePolicy(AdminPolicyType policyType, Long policyId);
}
