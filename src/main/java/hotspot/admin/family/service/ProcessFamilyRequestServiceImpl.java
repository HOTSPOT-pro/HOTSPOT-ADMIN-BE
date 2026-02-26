package hotspot.admin.family.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.code.FamilyErrorCode;
import hotspot.admin.family.controller.port.ProcessFamilyRequestService;
import hotspot.admin.family.domain.ApplyType;
import hotspot.admin.family.domain.FamilyApplyStatus;
import hotspot.admin.family.domain.PriorityType;
import hotspot.admin.family.service.dto.FamilyAddApprovalInfo;
import hotspot.admin.family.service.port.FamilyApplyRepository;
import hotspot.admin.family.service.port.FamilyRepository;
import hotspot.admin.family.service.port.FamilySubRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProcessFamilyRequestServiceImpl implements ProcessFamilyRequestService {

    private static final long SHARED_DATA_PER_MEMBER_KB = 5L * 1024 * 1024;
    private static final int UNUSED_PRIORITY_ORDER = -1;

    private final FamilyApplyRepository familyApplyRepository;
    private final FamilyRepository familyRepository;
    private final FamilySubRepository familySubRepository;

    /** 가족 요청을 승인 상태로 전환한다. */
    @Override
    @Transactional
    public void approve(ApplyType applyType, Long requestId) {
        processRequest(applyType, requestId, FamilyApplyStatus.APPROVED);
        if (applyType == ApplyType.ADD) {
            applyAddApproval(requestId);
        }
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
        int updatedCount = familyApplyRepository.updateFamilyRequestStatus(
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
        if (!familyApplyRepository.existsFamilyRequest(requestId, applyType)) {
            throw new ApplicationException(FamilyErrorCode.FAMILY_REQUEST_NOT_FOUND);
        }
        throw new ApplicationException(FamilyErrorCode.FAMILY_REQUEST_NOT_PENDING);
    }

    /** ADD 승인 시 family_sub 삽입 후 family/family_sub 요약 값을 동기화한다. */
    private void applyAddApproval(Long requestId) {
        FamilyAddApprovalInfo request = familyApplyRepository.findAddApprovalInfo(requestId)
                .orElseThrow(() -> new ApplicationException(FamilyErrorCode.FAMILY_REQUEST_NOT_FOUND));

        Long familyId = request.familyId();
        Long targetSubId = request.targetSubId();
        PriorityType priorityType = familyRepository.findFamilyPriorityType(familyId)
                .orElseThrow(() -> new ApplicationException(FamilyErrorCode.FAMILY_NOT_FOUND));

        if (!familySubRepository.existsFamilySub(familyId, targetSubId)) {
            int insertPriority = resolveInsertPriority(priorityType, familyId);
            familySubRepository.saveFamilySub(
                    familyId,
                    targetSubId,
                    request.targetFamilyRole(),
                    insertPriority,
                    0L
            );
        }

        int memberCount = familySubRepository.countActiveMembers(familyId);
        long familyDataAmount = memberCount * SHARED_DATA_PER_MEMBER_KB;

        familyRepository.updateFamilySummary(familyId, memberCount, familyDataAmount);
        familySubRepository.updateDataLimit(familyId, familyDataAmount);

        if (priorityType == PriorityType.FIFO) {
            familySubRepository.updatePriority(familyId, UNUSED_PRIORITY_ORDER);
        }
    }

    /** 우선순위 유형에 따라 신규 구성원 우선순위 값을 계산한다. */
    private int resolveInsertPriority(PriorityType priorityType, Long familyId) {
        if (priorityType == PriorityType.FIFO) {
            return UNUSED_PRIORITY_ORDER;
        }
        return familySubRepository.findMaxPriority(familyId) + 1;
    }
}
