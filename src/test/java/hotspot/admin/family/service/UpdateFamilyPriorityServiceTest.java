package hotspot.admin.family.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.code.FamilyErrorCode;
import hotspot.admin.family.controller.request.MemberPriorityRequest;
import hotspot.admin.family.domain.PriorityType;
import hotspot.admin.family.infrastructure.query.dto.FamilyControlMemberRow;
import hotspot.admin.family.service.port.FamilyRepository;
import hotspot.admin.family.service.port.FamilySubQueryRepository;
import hotspot.admin.family.service.port.FamilySubRepository;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class UpdateFamilyPriorityServiceTest {

    @Mock
    private FamilyRepository familyRepository;

    @Mock
    private FamilySubRepository familySubRepository;

    @Mock
    private FamilySubQueryRepository familySubQueryRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private UpdateFamilyPriorityServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UpdateFamilyPriorityServiceImpl(
                familyRepository,
                familySubRepository,
                familySubQueryRepository,
                applicationEventPublisher
        );
    }

    @Test
    @DisplayName("PRIORITY로 변경 시 요청 우선순위를 그대로 반영한다")
    void updatePriorityTypeToPrioritySuccess() {
        when(familyRepository.updateFamilyPriorityType(1L, PriorityType.PRIORITY)).thenReturn(1);
        when(familySubQueryRepository.findFamilyControlMembers(1L))
                .thenReturn(List.of(
                        FamilyControlMemberRow.builder()
                                .subId(10L)
                                .build(),
                        FamilyControlMemberRow.builder()
                                .subId(20L)
                                .build(),
                        FamilyControlMemberRow.builder()
                                .subId(30L)
                                .build()
                ));
        List<MemberPriorityRequest> memberPriorities = List.of(
                new MemberPriorityRequest(30L, 3),
                new MemberPriorityRequest(10L, 1),
                new MemberPriorityRequest(20L, 2)
        );

        service.updatePriorityType(1L, PriorityType.PRIORITY, memberPriorities);

        verify(familyRepository).updateFamilyPriorityType(1L, PriorityType.PRIORITY);
        InOrder inOrder = inOrder(familySubRepository);
        inOrder.verify(familySubRepository).updateMemberPriority(1L, 10L, 1);
        inOrder.verify(familySubRepository).updateMemberPriority(1L, 20L, 2);
        inOrder.verify(familySubRepository).updateMemberPriority(1L, 30L, 3);
    }

    @Test
    @DisplayName("FIFO로 변경 시 구성원 우선순위를 -1로 초기화한다")
    void updatePriorityTypeToFifoSuccess() {
        when(familyRepository.updateFamilyPriorityType(1L, PriorityType.FIFO)).thenReturn(1);

        service.updatePriorityType(1L, PriorityType.FIFO, null);

        verify(familyRepository).updateFamilyPriorityType(1L, PriorityType.FIFO);
        verify(familySubRepository).updatePriority(1L, -1);
    }

    @Test
    @DisplayName("가족이 없으면 FAMILY_NOT_FOUND")
    void familyNotFound() {
        when(familyRepository.updateFamilyPriorityType(999L, PriorityType.FIFO)).thenReturn(0);

        assertThatThrownBy(() -> service.updatePriorityType(999L, PriorityType.FIFO, null))
                .isInstanceOf(ApplicationException.class)
                .matches(ex -> ((ApplicationException) ex).getCode() == FamilyErrorCode.FAMILY_NOT_FOUND);
    }

    @Test
    @DisplayName("PRIORITY에서 우선순위 값 누락 시 예외")
    void missingPriorityValues() {
        when(familyRepository.updateFamilyPriorityType(1L, PriorityType.PRIORITY)).thenReturn(1);

        assertThatThrownBy(() -> service.updatePriorityType(1L, PriorityType.PRIORITY, null))
                .isInstanceOf(ApplicationException.class)
                .matches(ex -> ((ApplicationException) ex).getCode() == FamilyErrorCode.MISSING_PRIORITY_VALUES);
    }

    @Test
    @DisplayName("PRIORITY에서 동일 구성원 중복 입력 시 예외")
    void duplicatePriorityMember() {
        when(familyRepository.updateFamilyPriorityType(1L, PriorityType.PRIORITY)).thenReturn(1);

        List<MemberPriorityRequest> memberPriorities = List.of(
                new MemberPriorityRequest(12L, 1),
                new MemberPriorityRequest(12L, 2)
        );

        assertThatThrownBy(() -> service.updatePriorityType(1L, PriorityType.PRIORITY, memberPriorities))
                .isInstanceOf(ApplicationException.class)
                .matches(ex -> ((ApplicationException) ex).getCode() == FamilyErrorCode.DUPLICATE_PRIORITY_MEMBER);
    }
}
