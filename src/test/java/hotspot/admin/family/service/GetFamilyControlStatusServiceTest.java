package hotspot.admin.family.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

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
import hotspot.admin.family.controller.response.FamilyControlStatusResponse;
import hotspot.admin.family.domain.FamilyRole;
import hotspot.admin.family.domain.PriorityType;
import hotspot.admin.family.infrastructure.query.dto.FamilyControlMemberRow;
import hotspot.admin.family.service.port.FamilyRepository;
import hotspot.admin.family.service.port.FamilySubQueryRepository;

@ExtendWith(MockitoExtension.class)
class GetFamilyControlStatusServiceTest {

    @Mock
    private FamilyRepository familyRepository;

    @Mock
    private FamilySubQueryRepository familySubQueryRepository;

    private GetFamilyControlStatusServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GetFamilyControlStatusServiceImpl(familyRepository, familySubQueryRepository);
    }

    @Test
    @DisplayName("제어 기능 조회 성공 - PRIORITY")
    void getFamilyControlStatusPrioritySuccess() {
        when(familyRepository.findFamilyPriorityType(1L))
                .thenReturn(Optional.of(PriorityType.PRIORITY));
        when(familySubQueryRepository.findFamilyControlMembers(1L))
                .thenReturn(List.of(
                        FamilyControlMemberRow.builder()
                                .subId(10L)
                                .memberName("대표")
                                .familyRole(FamilyRole.OWNER)
                                .blocked(false)
                                .dataLimit(2048L)
                                .priority(1)
                                .build(),
                        FamilyControlMemberRow.builder()
                                .subId(11L)
                                .memberName("부모")
                                .familyRole(FamilyRole.PARENT)
                                .blocked(true)
                                .dataLimit(1024L)
                                .priority(2)
                                .build(),
                        FamilyControlMemberRow.builder()
                                .subId(12L)
                                .memberName("자녀")
                                .familyRole(FamilyRole.CHILD)
                                .blocked(false)
                                .dataLimit(512L)
                                .priority(3)
                                .build()
                ));

        FamilyControlStatusResponse response = service.getFamilyControlStatus(1L);

        assertThat(response.priorityType()).isEqualTo(PriorityType.PRIORITY);
        assertThat(response.members()).hasSize(3);
        assertThat(response.members().get(0).subId()).isEqualTo(10L);
        assertThat(response.members().get(0).isParent()).isNull();
        assertThat(response.members().get(1).isParent()).isTrue();
        assertThat(response.members().get(2).isParent()).isFalse();
        assertThat(response.members().get(0).dataLimitGb()).isEqualTo(2048D / (1024D * 1024D));
        assertThat(response.members().get(1).dataLimitGb()).isEqualTo(1024D / (1024D * 1024D));
        assertThat(response.members().get(0).priorityOrder()).isEqualTo(1);
    }

    @Test
    @DisplayName("FIFO면 우선순위 순서는 모두 -1")
    void fifoThenPriorityMinusOne() {
        when(familyRepository.findFamilyPriorityType(2L))
                .thenReturn(Optional.of(PriorityType.FIFO));
        when(familySubQueryRepository.findFamilyControlMembers(2L))
                .thenReturn(List.of(
                        FamilyControlMemberRow.builder()
                                .subId(20L)
                                .memberName("부모")
                                .familyRole(FamilyRole.PARENT)
                                .blocked(false)
                                .dataLimit(1024L)
                                .priority(99)
                                .build()
                ));

        FamilyControlStatusResponse response = service.getFamilyControlStatus(2L);

        assertThat(response.members().get(0).priorityOrder()).isEqualTo(-1);
    }

    @Test
    @DisplayName("가족이 없으면 FAMILY_NOT_FOUND")
    void familyNotFound() {
        when(familyRepository.findFamilyPriorityType(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getFamilyControlStatus(999L))
                .isInstanceOf(ApplicationException.class)
                .matches(ex -> ((ApplicationException) ex).getCode() == FamilyErrorCode.FAMILY_NOT_FOUND);
    }
}
