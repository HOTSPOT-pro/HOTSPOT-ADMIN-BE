package hotspot.admin.family.service.port;

import java.util.Set;

public interface FamilyPolicyAssignmentRepository {
    Set<Long> findExistingTimePolicyIds(Long familyId, Set<Long> policyIds);

    Set<Long> findExistingAppPolicyIds(Set<Long> policyIds);

    int updateMemberTimePolicyActive(Long subId, Long policyId, boolean isActive);

    void insertMemberTimePolicy(Long subId, Long policyId, boolean isActive);

    int updateMemberAppPolicyActive(Long subId, Long policyId, boolean isActive);

    void insertMemberAppPolicy(Long subId, Long policyId);

    void bulkDeactivateTimePoliciesByIds(Set<Long> policySubIds);
}
