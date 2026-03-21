package hotspot.admin.policy.controller.port;

import hotspot.admin.policy.controller.request.CreateAppPolicyRequest;
import hotspot.admin.policy.controller.request.CreateTimePolicyRequest;
import hotspot.admin.policy.controller.response.CreateAppPolicyResponse;
import hotspot.admin.policy.controller.response.CreateTimePolicyResponse;

public interface CreatePolicyService {

    CreateTimePolicyResponse createTimePolicy(CreateTimePolicyRequest request);

    CreateAppPolicyResponse createAppPolicy(CreateAppPolicyRequest request);
}
