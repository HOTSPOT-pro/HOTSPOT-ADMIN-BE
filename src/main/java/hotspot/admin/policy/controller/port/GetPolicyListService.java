package hotspot.admin.policy.controller.port;

import hotspot.admin.policy.controller.request.PolicyListRequest;
import hotspot.admin.policy.controller.response.AppPolicyListResponse;
import hotspot.admin.policy.controller.response.TimePolicyListResponse;

public interface GetPolicyListService {

    TimePolicyListResponse getTimePolicies(PolicyListRequest request);

    AppPolicyListResponse getAppPolicies(PolicyListRequest request);
}
