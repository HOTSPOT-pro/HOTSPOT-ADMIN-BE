package hotspot.admin.family.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.code.FamilyErrorCode;
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
import hotspot.admin.outbox.consistencyOutbox.publisher.family.FamilyEventPublisher;

@ExtendWith(MockitoExtension.class)
class ProcessFamilyRequestServiceTest {

    @Mock
    private FamilyApplyRepository familyApplyRepository;
    @Mock
    private FamilyApplyQueryRepository familyApplyQueryRepository;
    @Mock
    private FamilyRemoveScheduleRepository familyRemoveScheduleRepository;
    @Mock
    private FamilyRepository familyRepository;
    @Mock
    private FamilySubRepository familySubRepository;
    @Mock
    private FamilyRequestOutboxPublisher familyRequestOutboxPublisher;
    @Mock
    private FamilyEventPublisher familyEventPublisher;

    private ProcessFamilyRequestServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProcessFamilyRequestServiceImpl(
                familyApplyRepository,
                familyApplyQueryRepository,
                familyRemoveScheduleRepository,
                familyRepository,
                familySubRepository,
                familyRequestOutboxPublisher,
                familyEventPublisher
        );
    }

    @Test
    @DisplayName("대기중 CREATE 신청 승인 성공")
    void approveCreateSuccess() {
        FamilyApply familyApply = FamilyApply.builder()
                .familyApplyId(17L)
                .requesterSubId(30L)
                .familyId(99L)
                .applyType(ApplyType.CREATE)
                .status(FamilyApplyStatus.APPROVED)
                .createdTime(LocalDateTime.now())
                .modifiedTime(LocalDateTime.now())
                .build();

        when(familyApplyRepository.updateFamilyRequestStatus(17L, ApplyType.CREATE, FamilyApplyStatus.PENDING,
                FamilyApplyStatus.APPROVED)).thenReturn(1);
        when(familyApplyRepository.findFamilyRequestOutboxInfo(17L, ApplyType.CREATE))
                .thenReturn(Optional.of(new FamilyRequestOutboxInfo(familyApply, List.of("member-a", "member-b"))));
        when(familyApplyQueryRepository.findApprovalTargetInfos(17L, ApplyType.CREATE))
                .thenReturn(List.of(
                        FamilyApprovalTargetInfo.builder()
                                .familyId(null)
                                .targetSubId(31L)
                                .targetFamilyRole(FamilyRole.PARENT)
                                .build(),
                        FamilyApprovalTargetInfo.builder()
                                .familyId(null)
                                .targetSubId(32L)
                                .targetFamilyRole(FamilyRole.CHILD)
                                .build()
                ));
        when(familyApplyRepository.findRequesterSubId(17L, ApplyType.CREATE))
                .thenReturn(Optional.of(30L));
        when(familyRepository.createFamily(3, 15728640L, PriorityType.FIFO))
                .thenReturn(99L);
        when(familyApplyRepository.updateFamilyId(17L, ApplyType.CREATE, 99L))
                .thenReturn(1);

        service.approve(ApplyType.CREATE, 17L);

        verify(familyRequestOutboxPublisher)
                .publishApproved(eq(familyApply), eq(List.of("member-a", "member-b")), eq(99L));
        verify(familySubRepository).saveFamilySub(99L, 30L, FamilyRole.OWNER, -1, 15728640L);
        verify(familySubRepository).saveFamilySub(99L, 31L, FamilyRole.PARENT, -1, 15728640L);
        verify(familySubRepository).saveFamilySub(99L, 32L, FamilyRole.CHILD, -1, 15728640L);
        verify(familyEventPublisher).publishFamilyCreated(eq(99L), eq(List.of(31L, 32L, 30L)));


    }

    @Test
    @DisplayName("대기중 ADD 신청 승인 성공")
    void approveAddSuccess() {
        FamilyApply familyApply = FamilyApply.builder()
                .familyApplyId(7L)
                .requesterSubId(10L)
                .familyId(3L)
                .applyType(ApplyType.ADD)
                .status(FamilyApplyStatus.APPROVED)
                .createdTime(LocalDateTime.now())
                .modifiedTime(LocalDateTime.now())
                .build();

        when(familyApplyRepository.updateFamilyRequestStatus(7L, ApplyType.ADD, FamilyApplyStatus.PENDING,
                FamilyApplyStatus.APPROVED)).thenReturn(1);
        when(familyApplyRepository.findFamilyRequestOutboxInfo(7L, ApplyType.ADD))
                .thenReturn(Optional.of(new FamilyRequestOutboxInfo(familyApply, List.of("target-name"))));
        when(familyApplyQueryRepository.findApprovalTargetInfos(7L, ApplyType.ADD))
                .thenReturn(List.of(FamilyApprovalTargetInfo.builder()
                        .familyId(3L)
                        .targetSubId(100L)
                        .targetFamilyRole(FamilyRole.CHILD)
                        .build()));
        when(familyRepository.findFamilyPriorityType(3L)).thenReturn(Optional.of(PriorityType.FIFO));
        when(familySubRepository.existsFamilySub(3L, 100L)).thenReturn(false);
        when(familySubRepository.countActiveMembers(3L)).thenReturn(3);

        service.approve(ApplyType.ADD, 7L);

        verify(familyRequestOutboxPublisher)
                .publishApproved(eq(familyApply), eq(List.of("target-name")), eq(3L));
        verify(familySubRepository).saveFamilySub(3L, 100L, FamilyRole.CHILD, -1, 0L);
        verify(familyRepository).updateFamilySummary(3L, 3, 15728640L);
        verify(familySubRepository).updateDataLimit(3L, 15728640L);
        verify(familySubRepository).updatePriority(3L, -1);
        verify(familyEventPublisher).publishMemberAdded(eq(3L), eq(100L));
    }

    @Test
    @DisplayName("REMOVE 승인 시 삭제 스케줄을 등록한다")
    void approveRemoveSchedules() {
        FamilyApply familyApply = FamilyApply.builder()
                .familyApplyId(5L)
                .requesterSubId(20L)
                .familyId(6L)
                .applyType(ApplyType.REMOVE)
                .status(FamilyApplyStatus.APPROVED)
                .createdTime(LocalDateTime.now())
                .modifiedTime(LocalDateTime.now())
                .build();

        when(familyApplyRepository.updateFamilyRequestStatus(5L, ApplyType.REMOVE, FamilyApplyStatus.PENDING,
                FamilyApplyStatus.APPROVED)).thenReturn(1);
        when(familyApplyRepository.findFamilyRequestOutboxInfo(5L, ApplyType.REMOVE))
                .thenReturn(Optional.of(new FamilyRequestOutboxInfo(familyApply, List.of("remove-target"))));
        when(familyApplyQueryRepository.findApprovalTargetInfos(5L, ApplyType.REMOVE))
                .thenReturn(List.of(FamilyApprovalTargetInfo.builder()
                        .familyId(6L)
                        .targetSubId(200L)
                        .targetFamilyRole(FamilyRole.CHILD)
                        .build()));
        when(familyRemoveScheduleRepository.findAllByTargetSubIdInAndStatus(any(), any()))
                .thenReturn(List.of(FamilyRemoveSchedule.builder()
                        .familyRemoveScheduleId(1L)
                        .targetSubId(999L)
                        .familyId(6L)
                        .status(DeleteStatus.SCHEDULED)
                        .build()));

        service.approve(ApplyType.REMOVE, 5L);

        verify(familyRequestOutboxPublisher)
                .publishApproved(eq(familyApply), eq(List.of("remove-target")), eq(6L));
        verify(familyRemoveScheduleRepository).saveAll(any());
        verifyNoInteractions(familyRepository, familySubRepository);
    }

    @Test
    @DisplayName("대기중 요청 반려 성공")
    void rejectSuccess() {
        FamilyApply familyApply = FamilyApply.builder()
                .familyApplyId(8L)
                .requesterSubId(40L)
                .familyId(9L)
                .applyType(ApplyType.REMOVE)
                .status(FamilyApplyStatus.REJECTED)
                .createdTime(LocalDateTime.now())
                .modifiedTime(LocalDateTime.now())
                .build();

        when(familyApplyRepository.updateFamilyRequestStatus(8L, ApplyType.REMOVE, FamilyApplyStatus.PENDING,
                FamilyApplyStatus.REJECTED)).thenReturn(1);
        when(familyApplyRepository.findFamilyRequestOutboxInfo(8L, ApplyType.REMOVE))
                .thenReturn(Optional.of(new FamilyRequestOutboxInfo(familyApply, List.of("reject-target"))));

        service.reject(ApplyType.REMOVE, 8L);

        verify(familyRequestOutboxPublisher)
                .publishRejected(eq(familyApply), eq(List.of("reject-target")), eq(9L));
        verifyNoInteractions(
                familyApplyQueryRepository,
                familyRemoveScheduleRepository,
                familyRepository,
                familySubRepository
        );
    }

    @Test
    @DisplayName("요청이 존재하지 않으면 예외")
    void requestNotFoundThenException() {
        when(familyApplyRepository.updateFamilyRequestStatus(9L, ApplyType.REMOVE, FamilyApplyStatus.PENDING,
                FamilyApplyStatus.REJECTED)).thenReturn(0);
        when(familyApplyRepository.existsFamilyRequest(9L, ApplyType.REMOVE)).thenReturn(false);

        assertThatThrownBy(() -> service.reject(ApplyType.REMOVE, 9L))
                .isInstanceOf(ApplicationException.class)
                .matches(ex -> ((ApplicationException) ex).getCode() == FamilyErrorCode.FAMILY_REQUEST_NOT_FOUND);
        verify(familyRequestOutboxPublisher, never()).publishRejected(any(), any(), any());
    }

    @Test
    @DisplayName("이미 처리된 요청이면 예외")
    void requestNotPendingThenException() {
        when(familyApplyRepository.updateFamilyRequestStatus(9L, ApplyType.REMOVE, FamilyApplyStatus.PENDING,
                FamilyApplyStatus.REJECTED)).thenReturn(0);
        when(familyApplyRepository.existsFamilyRequest(9L, ApplyType.REMOVE)).thenReturn(true);

        assertThatThrownBy(() -> service.reject(ApplyType.REMOVE, 9L))
                .isInstanceOf(ApplicationException.class)
                .matches(ex -> ((ApplicationException) ex).getCode() == FamilyErrorCode.FAMILY_REQUEST_NOT_PENDING);
        verify(familyRequestOutboxPublisher, never()).publishRejected(any(), any(), any());
    }
}
