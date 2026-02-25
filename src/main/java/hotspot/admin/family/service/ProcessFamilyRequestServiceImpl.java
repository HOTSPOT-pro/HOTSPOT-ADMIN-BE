package hotspot.admin.family.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.code.FamilyErrorCode;
import hotspot.admin.family.controller.port.ProcessFamilyRequestService;
import hotspot.admin.family.domain.ApplyType;
import hotspot.admin.family.domain.FamilyApplyStatus;
import hotspot.admin.family.service.port.FamilyRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProcessFamilyRequestServiceImpl implements ProcessFamilyRequestService {

    private final FamilyRepository familyRepository;

    @Override
    @Transactional
    public void approve(ApplyType applyType, Long requestId) {
        processRequest(applyType, requestId, FamilyApplyStatus.APPROVED);
    }

    @Override
    @Transactional
    public void reject(ApplyType applyType, Long requestId) {
        processRequest(applyType, requestId, FamilyApplyStatus.REJECTED);
    }

    private void processRequest(
            ApplyType applyType,
            Long requestId,
            FamilyApplyStatus nextStatus
    ) {
        int updatedCount = familyRepository.updateFamilyRequestStatus(
                requestId,
                applyType,
                FamilyApplyStatus.PENDING,
                nextStatus
        );

        if (updatedCount == 0) {
            handleNotUpdated(requestId, applyType);
        }
    }

    private void handleNotUpdated(Long requestId, ApplyType applyType) {
        if (!familyRepository.existsFamilyRequest(requestId, applyType)) {
            throw new ApplicationException(FamilyErrorCode.FAMILY_REQUEST_NOT_FOUND);
        }
        throw new ApplicationException(FamilyErrorCode.FAMILY_REQUEST_NOT_PENDING);
    }
}
