package hotspot.admin.family.controller.request;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;

public record UpdateFamilyMemberPolicyStatusRequest(
        @Valid List<PolicyActiveRequest> timePolicies,
        @Valid List<PolicyActiveRequest> appPolicies
) {

    @AssertTrue(message = "timePolicies 또는 appPolicies 중 하나 이상은 입력해야 합니다.")
    public boolean hasAnyField() {
        return !isNullOrEmpty(timePolicies) || !isNullOrEmpty(appPolicies);
    }

    @AssertTrue(message = "timePolicies에 중복된 policyId가 존재합니다.")
    public boolean hasUniqueTimePolicies() {
        return hasUniquePolicyIds(timePolicies);
    }

    @AssertTrue(message = "appPolicies에 중복된 policyId가 존재합니다.")
    public boolean hasUniqueAppPolicies() {
        return hasUniquePolicyIds(appPolicies);
    }

    private static boolean isNullOrEmpty(List<PolicyActiveRequest> policies) {
        return policies == null || policies.isEmpty();
    }

    private static boolean hasUniquePolicyIds(List<PolicyActiveRequest> policies) {
        if (isNullOrEmpty(policies)) {
            return true;
        }

        Set<Long> uniqueIds = policies.stream()
                .map(PolicyActiveRequest::policyId)
                .collect(Collectors.toSet());
        return uniqueIds.size() == policies.size();
    }
}
