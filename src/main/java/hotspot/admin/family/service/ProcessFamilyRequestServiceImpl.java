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

    /** 가족 요청을 승인 상태로 전환한다. */
    @Override
    @Transactional
    public void approve(ApplyType applyType, Long requestId) {
        processRequest(applyType, requestId, FamilyApplyStatus.APPROVED);
    }

    /** 가족 요청을 반려 상태로 전환한다. */
    @Override
    @Transactional
    public void reject(ApplyType applyType, Long requestId) {
        processRequest(applyType, requestId, FamilyApplyStatus.REJECTED);
    }

    /** 대기중 요청만 목표 상태로 전환하며, 실패 시 원인을 예외로 구분한다. */
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

    /** 업데이트 실패 시 요청 미존재/상태 불일치 원인을 구분해 예외를 던진다. */
    private void handleNotUpdated(Long requestId, ApplyType applyType) {
        if (!familyRepository.existsFamilyRequest(requestId, applyType)) {
            throw new ApplicationException(FamilyErrorCode.FAMILY_REQUEST_NOT_FOUND);
        }
        throw new ApplicationException(FamilyErrorCode.FAMILY_REQUEST_NOT_PENDING);
    }
}
