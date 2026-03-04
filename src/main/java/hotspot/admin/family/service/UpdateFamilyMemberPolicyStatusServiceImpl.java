package hotspot.admin.family.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.code.FamilyErrorCode;
import hotspot.admin.common.exception.code.PolicyErrorCode;
import hotspot.admin.family.controller.port.UpdateFamilyMemberPolicyStatusService;
import hotspot.admin.family.controller.request.PolicyActiveRequest;
import hotspot.admin.family.service.port.FamilyPolicyAssignmentRepository;
import hotspot.admin.family.service.port.FamilyRepository;
import hotspot.admin.family.service.port.FamilySubRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateFamilyMemberPolicyStatusServiceImpl implements UpdateFamilyMemberPolicyStatusService {

    private final FamilyRepository familyRepository;
    private final FamilySubRepository familySubRepository;
    private final FamilyPolicyAssignmentRepository familyPolicyAssignmentRepository;

    @Transactional
    @Override
    public void updateMemberTimePolicyStatus(
            Long familyId,
            Long subId,
            List<PolicyActiveRequest> policies
    ) {
        validateFamilyAndMember(familyId, subId);
        validateTimePolicies(familyId, policies);
        updateTimePolicies(subId, policies);
    }

    @Transactional
    @Override
    public void updateMemberAppPolicyStatus(
            Long familyId,
            Long subId,
            List<PolicyActiveRequest> policies
    ) {
        validateFamilyAndMember(familyId, subId);
        validateAppPolicies(policies);
        updateAppPolicies(subId, policies);
    }

    private void validateTimePolicies(Long familyId, List<PolicyActiveRequest> policies) {
        Set<Long> requestedIds = toPolicyIdSet(policies);
        Set<Long> existingIds = familyPolicyAssignmentRepository.findExistingTimePolicyIds(familyId, requestedIds);
        if (!existingIds.containsAll(requestedIds)) {
            throw new ApplicationException(PolicyErrorCode.POLICY_NOT_FOUND);
        }
    }

    private void validateAppPolicies(List<PolicyActiveRequest> policies) {
        Set<Long> requestedIds = toPolicyIdSet(policies);
        Set<Long> existingIds = familyPolicyAssignmentRepository.findExistingAppPolicyIds(requestedIds);
        if (!existingIds.containsAll(requestedIds)) {
            throw new ApplicationException(PolicyErrorCode.POLICY_NOT_FOUND);
        }
    }

    private void validateFamilyAndMember(Long familyId, Long subId) {
        if (!familyRepository.existsFamilyById(familyId)) {
            throw new ApplicationException(FamilyErrorCode.FAMILY_NOT_FOUND);
        }
        if (!familySubRepository.existsFamilySub(familyId, subId)) {
            throw new ApplicationException(FamilyErrorCode.FAMILY_MEMBER_NOT_FOUND);
        }
    }

    private void updateTimePolicies(Long subId, List<PolicyActiveRequest> policies) {
        for (PolicyActiveRequest policy : policies) {
            int updated = familyPolicyAssignmentRepository.updateMemberTimePolicyActive(
                    subId,
                    policy.policyId(),
                    policy.isActive()
            );

            if (updated == 0 && Boolean.TRUE.equals(policy.isActive())) {
                familyPolicyAssignmentRepository.insertMemberTimePolicy(subId, policy.policyId(), true);
            }
        }
    }

    private void updateAppPolicies(Long subId, List<PolicyActiveRequest> policies) {
        for (PolicyActiveRequest policy : policies) {
            int updated = familyPolicyAssignmentRepository.updateMemberAppPolicyActive(
                    subId,
                    policy.policyId(),
                    policy.isActive()
            );

            if (updated == 0 && Boolean.TRUE.equals(policy.isActive())) {
                familyPolicyAssignmentRepository.insertMemberAppPolicy(subId, policy.policyId());
            }
        }
    }

    private Set<Long> toPolicyIdSet(List<PolicyActiveRequest> policies) {
        return policies.stream()
                .map(PolicyActiveRequest::policyId)
                .collect(Collectors.toSet());
    }
}
