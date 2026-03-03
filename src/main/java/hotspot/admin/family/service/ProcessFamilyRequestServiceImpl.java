package hotspot.admin.family.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.code.FamilyErrorCode;
import hotspot.admin.common.exception.code.OutboxErrorCode;
import hotspot.admin.family.controller.port.ProcessFamilyRequestService;
import hotspot.admin.family.domain.ApplyType;
import hotspot.admin.family.domain.DeleteStatus;
import hotspot.admin.family.domain.FamilyApply;
import hotspot.admin.family.domain.FamilyApplyStatus;
import hotspot.admin.family.domain.FamilyRemoveSchedule;
import hotspot.admin.family.domain.FamilyRole;
import hotspot.admin.family.domain.PriorityType;
import hotspot.admin.family.infrastructure.query.dto.FamilyApprovalTargetInfo;
import hotspot.admin.family.outbox.FamilyRequestOutboxPublisher;
import hotspot.admin.family.service.dto.FamilyRequestOutboxInfo;
import hotspot.admin.family.service.port.FamilyApplyQueryRepository;
import hotspot.admin.family.service.port.FamilyApplyRepository;
import hotspot.admin.family.service.port.FamilyRemoveScheduleRepository;
import hotspot.admin.family.service.port.FamilyRepository;
import hotspot.admin.family.service.port.FamilySubRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProcessFamilyRequestServiceImpl implements ProcessFamilyRequestService {

    private static final long KB_PER_GB = 1024L * 1024L;
    private static final long SHARED_DATA_PER_MEMBER_GB = 5L;
    private static final long SHARED_DATA_PER_MEMBER_KB = SHARED_DATA_PER_MEMBER_GB * KB_PER_GB;
    private static final int REMOVE_SCHEDULE_MONTH_OFFSET = 1;
    private static final long INITIAL_DATA_LIMIT = 0L;
    private static final int UNUSED_PRIORITY_ORDER = -1;

    private final FamilyApplyRepository familyApplyRepository;
    private final FamilyApplyQueryRepository familyApplyQueryRepository;
    private final FamilyRemoveScheduleRepository familyRemoveScheduleRepository;
    private final FamilyRepository familyRepository;
    private final FamilySubRepository familySubRepository;
    private final FamilyRequestOutboxPublisher familyRequestOutboxPublisher;

    /** 가족 요청을 승인 상태로 전환한다. */
    @Override
    @Transactional
    public void approve(ApplyType applyType, Long requestId) {
        processRequest(applyType, requestId, FamilyApplyStatus.APPROVED);
        applyApprovedSideEffect(applyType, requestId);
    }

    /** 가족 요청을 반려 상태로 전환한다. */
    @Override
    @Transactional
    public void reject(ApplyType applyType, Long requestId) {
        processRequest(applyType, requestId, FamilyApplyStatus.REJECTED);
    }

    /** 대기중 요청만 목표 상태로 전환하고, 실패 시 원인을 구분해 예외를 던진다. */
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
            return;
        }

        FamilyRequestOutboxInfo outboxInfo = familyApplyRepository.findFamilyRequestOutboxInfo(requestId, applyType)
                .orElseThrow(() -> new ApplicationException(FamilyErrorCode.FAMILY_REQUEST_NOT_FOUND));

        publishOutbox(nextStatus, outboxInfo.familyApply(), outboxInfo.targetName());
    }

    /** 업데이트 실패 시 요청 미존재/상태 불일치 원인을 구분해 예외를 던진다. */
    private void handleNotUpdated(Long requestId, ApplyType applyType) {
        if (!familyApplyRepository.existsFamilyRequest(requestId, applyType)) {
            throw new ApplicationException(FamilyErrorCode.FAMILY_REQUEST_NOT_FOUND);
        }
        throw new ApplicationException(FamilyErrorCode.FAMILY_REQUEST_NOT_PENDING);
    }

    /** 다음 상태가 승인/반려인지에 따라 해당 Outbox 알림을 발행하고, 그 외 상태면 예외를 발생시킨다. */
    private void publishOutbox(FamilyApplyStatus nextStatus, FamilyApply familyApply, String targetName) {
        if (nextStatus == FamilyApplyStatus.APPROVED) {
            familyRequestOutboxPublisher.publishApproved(familyApply, targetName);
            return;
        }
        if (nextStatus == FamilyApplyStatus.REJECTED) {
            familyRequestOutboxPublisher.publishRejected(familyApply, targetName);
            return;
        }
        throw new ApplicationException(OutboxErrorCode.FAMILY_REQUEST_EVENT_BUILD_FAILED);
    }

    /** 승인된 요청 타입에 따라 family/family_sub 반영을 수행한다. */
    private void applyApprovedSideEffect(ApplyType applyType, Long requestId) {
        List<FamilyApprovalTargetInfo> approvals = familyApplyQueryRepository.findApprovalTargetInfos(requestId, applyType);
        if (approvals.isEmpty()) {
            throw new ApplicationException(FamilyErrorCode.FAMILY_REQUEST_NOT_FOUND);
        }

        if (applyType == ApplyType.CREATE) {
            applyCreateApproval(requestId, approvals);
            return;
        }

        Long familyId = requireFamilyId(approvals.get(0).familyId());
        if (applyType == ApplyType.ADD) {
            applyAddApproval(familyId, approvals);
            return;
        }

        if (applyType == ApplyType.REMOVE) {
            applyRemoveApproval(familyId, approvals);
            return;
        }

        throw new ApplicationException(FamilyErrorCode.INVALID_APPLY_TYPE);
    }

    /** CREATE 승인 시 신규 family를 만들고 대상자들을 family_sub로 생성한다. */
    private void applyCreateApproval(Long requestId, List<FamilyApprovalTargetInfo> approvals) {
        Long requesterSubId = familyApplyRepository.findRequesterSubId(requestId, ApplyType.CREATE)
                .orElseThrow(() -> new ApplicationException(FamilyErrorCode.FAMILY_REQUEST_NOT_FOUND));

        List<FamilyApprovalTargetInfo> members = new ArrayList<>(approvals);
        boolean requesterExists = members.stream().anyMatch(item -> requesterSubId.equals(item.targetSubId()));
        if (!requesterExists) {
            members.add(FamilyApprovalTargetInfo.builder()
                    .familyId(null)
                    .targetSubId(requesterSubId)
                    .targetFamilyRole(FamilyRole.OWNER)
                    .build());
        }

        List<FamilyApprovalTargetInfo> distinctMembers = members.stream()
                .filter(item -> item.targetSubId() != null)
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toMap(
                                FamilyApprovalTargetInfo::targetSubId,
                                item -> item,
                                (existing, ignored) -> existing,
                                java.util.LinkedHashMap::new
                        ),
                        map -> new ArrayList<>(map.values())
                ));

        int memberCount = distinctMembers.size();
        long familyDataAmount = memberCount * SHARED_DATA_PER_MEMBER_KB;
        PriorityType priorityType = PriorityType.FIFO;
        Long familyId = familyRepository.createFamily(memberCount, familyDataAmount, priorityType);

        for (FamilyApprovalTargetInfo approval : distinctMembers) {
            FamilyRole role = requesterSubId.equals(approval.targetSubId())
                    ? FamilyRole.OWNER
                    : approval.targetFamilyRole();
            familySubRepository.saveFamilySub(
                    familyId,
                    approval.targetSubId(),
                    role,
                    UNUSED_PRIORITY_ORDER,
                    familyDataAmount
            );
        }
    }

    /** ADD 승인 시 family_sub 삽입 및 family/family_sub 요약 값을 동기화한다. */
    private void applyAddApproval(Long familyId, List<FamilyApprovalTargetInfo> approvals) {
        PriorityType priorityType = familyRepository.findFamilyPriorityType(familyId)
                .orElseThrow(() -> new ApplicationException(FamilyErrorCode.FAMILY_NOT_FOUND));

        for (FamilyApprovalTargetInfo request : approvals) {
            if (!familySubRepository.existsFamilySub(familyId, request.targetSubId())) {
                int insertPriority = resolveInsertPriority(priorityType, familyId);
                familySubRepository.saveFamilySub(
                        familyId,
                        request.targetSubId(),
                        request.targetFamilyRole(),
                        insertPriority,
                        INITIAL_DATA_LIMIT
                );
            }
        }

        int memberCount = familySubRepository.countActiveMembers(familyId);
        long familyDataAmount = memberCount * SHARED_DATA_PER_MEMBER_KB;

        familyRepository.updateFamilySummary(familyId, memberCount, familyDataAmount);
        familySubRepository.updateDataLimit(familyId, familyDataAmount);

        if (priorityType == PriorityType.FIFO) {
            familySubRepository.updatePriority(familyId, UNUSED_PRIORITY_ORDER);
        }
    }

    /** REMOVE 승인 시 삭제 배치 테이블(family_remove_schedule)에 일정을 등록한다. */
    private void applyRemoveApproval(Long familyId, List<FamilyApprovalTargetInfo> approvals) {
        List<Long> targetSubIds = approvals.stream()
                .map(FamilyApprovalTargetInfo::targetSubId)
                .filter(subId -> subId != null)
                .distinct()
                .toList();

        if (targetSubIds.isEmpty()) {
            return;
        }

        Set<Long> alreadyScheduledSubIds = familyRemoveScheduleRepository
                .findAllByTargetSubIdInAndStatus(targetSubIds, DeleteStatus.SCHEDULED)
                .stream()
                .filter(schedule -> familyId.equals(schedule.getFamilyId()))
                .map(FamilyRemoveSchedule::getTargetSubId)
                .collect(java.util.stream.Collectors.toSet());

        LocalDate scheduleDate = LocalDate.now()
                .plusMonths(REMOVE_SCHEDULE_MONTH_OFFSET)
                .withDayOfMonth(1);

        List<FamilyRemoveSchedule> newSchedules = targetSubIds.stream()
                .filter(subId -> !alreadyScheduledSubIds.contains(subId))
                .map(subId -> FamilyRemoveSchedule.builder()
                        .targetSubId(subId)
                        .familyId(familyId)
                        .status(DeleteStatus.SCHEDULED)
                        .scheduleDate(scheduleDate)
                        .build())
                .toList();

        familyRemoveScheduleRepository.saveAll(newSchedules);
    }

    private Long requireFamilyId(Long familyId) {
        if (familyId == null) {
            throw new ApplicationException(FamilyErrorCode.FAMILY_NOT_FOUND);
        }
        return familyId;
    }

    /** 우선순위 유형에 따라 신규 구성원 우선순위 값을 계산한다. */
    private int resolveInsertPriority(PriorityType priorityType, Long familyId) {
        if (priorityType == PriorityType.FIFO) {
            return UNUSED_PRIORITY_ORDER;
        }
        return familySubRepository.findMaxPriority(familyId) + 1;
    }
}
