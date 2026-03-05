package hotspot.admin.family.controller.request;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;

public record UpdateFamilyMemberPoliciesRequest(
        @Valid List<PolicyActiveRequest> policies
) {

    @AssertTrue(message = "policies는 1개 이상 입력해야 합니다.")
    public boolean hasAnyPolicy() {
        return policies != null && !policies.isEmpty();
    }

    @AssertTrue(message = "policies에 중복된 policyId가 존재합니다.")
    public boolean hasUniquePolicyIds() {
        if (policies == null || policies.isEmpty()) {
            return true;
        }

        Set<Long> uniqueIds = policies.stream()
                .map(PolicyActiveRequest::policyId)
                .collect(Collectors.toSet());
        return uniqueIds.size() == policies.size();
    }
}
