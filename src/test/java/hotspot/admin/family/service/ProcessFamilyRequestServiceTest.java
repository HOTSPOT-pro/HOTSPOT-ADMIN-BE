package hotspot.admin.family.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.code.FamilyErrorCode;
import hotspot.admin.family.domain.ApplyType;
import hotspot.admin.family.domain.FamilyApplyStatus;
import hotspot.admin.family.service.port.FamilyApplyRepository;

@ExtendWith(MockitoExtension.class)
class ProcessFamilyRequestServiceTest {

    @Mock
    private FamilyApplyRepository familyApplyRepository;

    private ProcessFamilyRequestServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProcessFamilyRequestServiceImpl(familyApplyRepository);
    }

    @Test
    @DisplayName("대기중 신청 요청 승인 성공")
    void approveSuccess() {
        when(familyApplyRepository.updateFamilyRequestStatus(
                7L,
                ApplyType.ADD,
                FamilyApplyStatus.PENDING,
                FamilyApplyStatus.APPROVED
        )).thenReturn(1);

        service.approve(ApplyType.ADD, 7L);

        verify(familyApplyRepository).updateFamilyRequestStatus(
                7L,
                ApplyType.ADD,
                FamilyApplyStatus.PENDING,
                FamilyApplyStatus.APPROVED
        );
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
    }
}
