package hotspot.admin.policy.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.admin.common.domain.DisplayIdType;
import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.code.PolicyErrorCode;
import hotspot.admin.common.util.DisplayIdFormatter;
import hotspot.admin.policy.controller.port.UpdatePolicyActiveService;
import hotspot.admin.policy.controller.response.UpdatePolicyActiveResponse;
import hotspot.admin.policy.domain.AdminPolicyType;
import hotspot.admin.policy.service.port.AppBlockedServiceRepository;
import hotspot.admin.policy.service.port.BlockPolicyRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdatePolicyActiveServiceImpl implements UpdatePolicyActiveService {

    private final BlockPolicyRepository blockPolicyRepository;
    private final AppBlockedServiceRepository appBlockedServiceRepository;

    @Transactional
    @Override
    public UpdatePolicyActiveResponse updatePolicyActive(AdminPolicyType policyType, Long policyId, Boolean isActive) {
        return switch (policyType) {
            case TIME -> updateTimePolicyActive(policyId, isActive);
            case APP -> updateAppPolicyActive(policyId, isActive);
        };
    }

    private UpdatePolicyActiveResponse updateTimePolicyActive(Long policyId, Boolean isActive) {
        int updatedRows = blockPolicyRepository.updateActiveById(policyId, isActive);
        if (updatedRows == 0) {
            throw new ApplicationException(PolicyErrorCode.POLICY_NOT_FOUND);
        }

        return UpdatePolicyActiveResponse.builder()
                .policyId(policyId)
                .displayId(DisplayIdFormatter.format(DisplayIdType.TIME_POLICY, policyId))
                .isActive(isActive)
                .build();
    }

    private UpdatePolicyActiveResponse updateAppPolicyActive(Long policyId, Boolean isActive) {
        int updatedRows = appBlockedServiceRepository.updateActiveById(policyId, isActive);
        if (updatedRows == 0) {
            throw new ApplicationException(PolicyErrorCode.POLICY_NOT_FOUND);
        }

        return UpdatePolicyActiveResponse.builder()
                .policyId(policyId)
                .displayId(DisplayIdFormatter.format(DisplayIdType.APP_POLICY, policyId))
                .isActive(isActive)
                .build();
    }
}
