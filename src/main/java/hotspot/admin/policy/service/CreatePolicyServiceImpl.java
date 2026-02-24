package hotspot.admin.policy.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.admin.appservice.domain.AppBlockedService;
import hotspot.admin.common.domain.DisplayIdType;
import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.code.PolicyErrorCode;
import hotspot.admin.common.util.DisplayIdFormatter;
import hotspot.admin.policy.controller.port.CreatePolicyService;
import hotspot.admin.policy.controller.request.CreateAppPolicyRequest;
import hotspot.admin.policy.controller.request.CreateTimePolicyRequest;
import hotspot.admin.policy.controller.response.CreateAppPolicyResponse;
import hotspot.admin.policy.controller.response.CreateTimePolicyResponse;
import hotspot.admin.policy.domain.BlockPolicy;
import hotspot.admin.policy.domain.PolicyType;
import hotspot.admin.policy.service.port.AppBlockedServiceRepository;
import hotspot.admin.policy.service.port.BlockPolicyRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreatePolicyServiceImpl implements CreatePolicyService {

    private final BlockPolicyRepository blockPolicyRepository;
    private final AppBlockedServiceRepository appBlockedServiceRepository;

    @Transactional
    @Override
    public CreateTimePolicyResponse createTimePolicy(CreateTimePolicyRequest request) {
        validateDuplicatePolicyNameType(request.policyName(), request.policyType());

        BlockPolicy saved = blockPolicyRepository.save(BlockPolicy.builder()
                .policyName(request.policyName())
                .policyDescription(request.policyDescription())
                .policyType(request.policyType())
                .policySnapshot(request.policySnapshot())
                .isActive(true)
                .isDeleted(false)
                .build());

        return CreateTimePolicyResponse.builder()
                .policyId(saved.getBlockPolicyId())
                .displayId(DisplayIdFormatter.format(DisplayIdType.TIME_POLICY, saved.getBlockPolicyId()))
                .isActive(saved.getIsActive())
                .build();
    }

    @Transactional
    @Override
    public CreateAppPolicyResponse createAppPolicy(CreateAppPolicyRequest request) {
        validateDuplicatePolicyCode(request.policyCode());

        AppBlockedService saved = appBlockedServiceRepository.save(AppBlockedService.builder()
                .blockedServiceName(request.policyName())
                .blockedServiceCode(request.policyCode())
                .isActive(true)
                .isDeleted(false)
                .build());

        return CreateAppPolicyResponse.builder()
                .policyId(saved.getAppBlockedServiceId())
                .displayId(DisplayIdFormatter.format(DisplayIdType.APP_POLICY, saved.getAppBlockedServiceId()))
                .isActive(saved.getIsActive())
                .build();
    }

    private void validateDuplicatePolicyNameType(String policyName, PolicyType policyType) {
        if (blockPolicyRepository.existsByPolicyNameAndPolicyType(policyName, policyType)) {
            throw new ApplicationException(PolicyErrorCode.DUPLICATE_POLICY_NAME_TYPE);
        }
    }

    private void validateDuplicatePolicyCode(String policyCode) {
        if (appBlockedServiceRepository.existsByBlockedServiceCode(policyCode)) {
            throw new ApplicationException(PolicyErrorCode.DUPLICATE_POLICY_CODE);
        }
    }
}
