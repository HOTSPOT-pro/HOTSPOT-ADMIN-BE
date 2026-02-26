package hotspot.admin.family.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
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
import hotspot.admin.family.domain.FamilyApply;
import hotspot.admin.family.domain.FamilyApplyStatus;
import hotspot.admin.family.domain.FamilyRole;
import hotspot.admin.family.domain.PriorityType;
import hotspot.admin.family.outbox.FamilyRequestOutboxPublisher;
import hotspot.admin.family.service.dto.FamilyAddApprovalInfo;
import hotspot.admin.family.service.dto.FamilyRequestOutboxInfo;
import hotspot.admin.family.service.port.FamilyApplyRepository;
import hotspot.admin.family.service.port.FamilyRepository;
import hotspot.admin.family.service.port.FamilySubRepository;

@ExtendWith(MockitoExtension.class)
class ProcessFamilyRequestServiceTest {

    @Mock
    private FamilyApplyRepository familyApplyRepository;

    @Mock
    private FamilyRepository familyRepository;

    @Mock
    private FamilySubRepository familySubRepository;

    @Mock
    private FamilyRequestOutboxPublisher familyRequestOutboxPublisher;

    private ProcessFamilyRequestServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProcessFamilyRequestServiceImpl(
                familyApplyRepository,
                familyRepository,
                familySubRepository,
                familyRequestOutboxPublisher
        );
    }

    @Test
    @DisplayName("대기중 신청 요청 승인 성공")
    void approveSuccess() {
        FamilyApply familyApply = FamilyApply.builder()
                .familyApplyId(7L)
                .requesterSubId(10L)
                .targetSubId(100L)
                .familyId(3L)
                .applyType(ApplyType.ADD)
                .targetFamilyRole(FamilyRole.CHILD)
                .status(FamilyApplyStatus.APPROVED)
                .createdTime(LocalDateTime.now())
                .modifiedTime(LocalDateTime.now())
                .build();

        when(familyApplyRepository.updateFamilyRequestStatus(
                7L,
                ApplyType.ADD,
                FamilyApplyStatus.PENDING,
                FamilyApplyStatus.APPROVED
        )).thenReturn(1);
        when(familyApplyRepository.findFamilyRequestOutboxInfo(7L, ApplyType.ADD))
                .thenReturn(Optional.of(new FamilyRequestOutboxInfo(familyApply, "target-name")));
        when(familyApplyRepository.findAddApprovalInfo(7L))
                .thenReturn(Optional.of(
                        FamilyAddApprovalInfo.builder()
                                .familyId(3L)
                                .targetSubId(100L)
                                .targetFamilyRole(FamilyRole.CHILD)
                                .build()
                ));
        when(familyRepository.findFamilyPriorityType(3L))
                .thenReturn(Optional.of(PriorityType.FIFO));
        when(familySubRepository.existsFamilySub(3L, 100L))
                .thenReturn(false);
        when(familySubRepository.countActiveMembers(3L))
                .thenReturn(3);

        service.approve(ApplyType.ADD, 7L);

        verify(familyApplyRepository).updateFamilyRequestStatus(
                7L,
                ApplyType.ADD,
                FamilyApplyStatus.PENDING,
                FamilyApplyStatus.APPROVED
        );
        verify(familyRequestOutboxPublisher).publishApproved(eq(familyApply), eq("target-name"));
        verify(familySubRepository).saveFamilySub(3L, 100L, FamilyRole.CHILD, -1, 0L);
        verify(familyRepository).updateFamilySummary(3L, 3, 15728640L);
        verify(familySubRepository).updateDataLimit(3L, 15728640L);
        verify(familySubRepository).updatePriority(3L, -1);
    }

    @Test
    @DisplayName("REMOVE 승인 시에는 후속 테이블 업데이트를 수행하지 않는다")
    void approveRemoveNoFollowUp() {
        FamilyApply familyApply = FamilyApply.builder()
                .familyApplyId(5L)
                .requesterSubId(20L)
                .targetSubId(200L)
                .familyId(6L)
                .applyType(ApplyType.REMOVE)
                .targetFamilyRole(FamilyRole.CHILD)
                .status(FamilyApplyStatus.APPROVED)
                .createdTime(LocalDateTime.now())
                .modifiedTime(LocalDateTime.now())
                .build();

        when(familyApplyRepository.updateFamilyRequestStatus(
                5L,
                ApplyType.REMOVE,
                FamilyApplyStatus.PENDING,
                FamilyApplyStatus.APPROVED
        )).thenReturn(1);
        when(familyApplyRepository.findFamilyRequestOutboxInfo(5L, ApplyType.REMOVE))
                .thenReturn(Optional.of(new FamilyRequestOutboxInfo(familyApply, "remove-target")));

        service.approve(ApplyType.REMOVE, 5L);

        verify(familyRequestOutboxPublisher).publishApproved(eq(familyApply), eq("remove-target"));
        verifyNoInteractions(familyRepository, familySubRepository);
    }

    @Test
    @DisplayName("요청이 존재하지 않으면 예외")
    void requestNotFoundThenException() {
        when(familyApplyRepository.updateFamilyRequestStatus(
                9L,
                ApplyType.REMOVE,
                FamilyApplyStatus.PENDING,
                FamilyApplyStatus.REJECTED
        )).thenReturn(0);
        when(familyApplyRepository.existsFamilyRequest(9L, ApplyType.REMOVE))
                .thenReturn(false);

        assertThatThrownBy(() -> service.reject(ApplyType.REMOVE, 9L))
                .isInstanceOf(ApplicationException.class)
                .matches(ex -> ((ApplicationException) ex).getCode() == FamilyErrorCode.FAMILY_REQUEST_NOT_FOUND);
        verify(familyRequestOutboxPublisher, never()).publishRejected(any(), any());
    }

    @Test
    @DisplayName("이미 처리된 요청이면 예외")
    void requestNotPendingThenException() {
        when(familyApplyRepository.updateFamilyRequestStatus(
                9L,
                ApplyType.REMOVE,
                FamilyApplyStatus.PENDING,
                FamilyApplyStatus.REJECTED
        )).thenReturn(0);
        when(familyApplyRepository.existsFamilyRequest(9L, ApplyType.REMOVE))
                .thenReturn(true);

        assertThatThrownBy(() -> service.reject(ApplyType.REMOVE, 9L))
                .isInstanceOf(ApplicationException.class)
                .matches(ex -> ((ApplicationException) ex).getCode() == FamilyErrorCode.FAMILY_REQUEST_NOT_PENDING);
        verify(familyRequestOutboxPublisher, never()).publishRejected(any(), any());
    }
}
