package hotspot.admin.policy.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.code.PolicyErrorCode;
import hotspot.admin.policy.controller.port.DeletePolicyService;
import hotspot.admin.policy.domain.AdminPolicyType;
import hotspot.admin.policy.service.port.AppBlockedServiceRepository;
import hotspot.admin.policy.service.port.BlockPolicyRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeletePolicyServiceImpl implements DeletePolicyService {

    private final BlockPolicyRepository blockPolicyRepository;
    private final AppBlockedServiceRepository appBlockedServiceRepository;

    @Transactional
    @Override
    public void deletePolicy(AdminPolicyType policyType, Long policyId) {
        int deletedRows = switch (policyType) {
            case TIME -> blockPolicyRepository.softDeleteById(policyId);
            case APP -> appBlockedServiceRepository.softDeleteById(policyId);
        };

        if (deletedRows == 0) {
            throw new ApplicationException(PolicyErrorCode.POLICY_NOT_FOUND);
        }
    }
}
