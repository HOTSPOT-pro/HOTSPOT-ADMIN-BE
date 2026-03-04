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
import hotspot.admin.family.domain.FamilyRole;
import hotspot.admin.family.service.port.FamilyRepository;
import hotspot.admin.family.service.port.FamilySubRepository;

@ExtendWith(MockitoExtension.class)
class UpdateFamilyMemberControlStatusServiceTest {

    @Mock
    private FamilyRepository familyRepository;

    @Mock
    private FamilySubRepository familySubRepository;

    private UpdateFamilyMemberControlStatusServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UpdateFamilyMemberControlStatusServiceImpl(familyRepository, familySubRepository);
    }

    @Test
    @DisplayName("구성원 제어 상태 수정 성공")
    void updateMemberControlStatusSuccess() {
        when(familyRepository.existsFamilyById(1L)).thenReturn(true);
        when(familySubRepository.existsFamilySub(1L, 101L)).thenReturn(true);
        when(familyRepository.findFamilyDataAmount(1L)).thenReturn(java.util.Optional.of(2097152L));
        when(familySubRepository.updateMemberDataLimit(1L, 101L, 1048576L)).thenReturn(1);
        when(familySubRepository.updateMemberBlocked(1L, 101L, true)).thenReturn(1);

        service.updateMemberControlStatus(1L, 101L, 1L, true, null);

        verify(familySubRepository).updateMemberDataLimit(1L, 101L, 1048576L);
        verify(familySubRepository).updateMemberBlocked(1L, 101L, true);
    }

    @Test
    @DisplayName("가족이 없으면 FAMILY_NOT_FOUND")
    void familyNotFound() {
        when(familyRepository.existsFamilyById(999L)).thenReturn(false);

        assertThatThrownBy(() -> service.updateMemberControlStatus(999L, 101L, 1L, null, null))
                .isInstanceOf(ApplicationException.class)
                .matches(ex -> ((ApplicationException) ex).getCode() == FamilyErrorCode.FAMILY_NOT_FOUND);
    }

    @Test
    @DisplayName("구성원이 없으면 FAMILY_MEMBER_NOT_FOUND")
    void memberNotFound() {
        when(familyRepository.existsFamilyById(1L)).thenReturn(true);
        when(familySubRepository.existsFamilySub(1L, 101L)).thenReturn(false);

        assertThatThrownBy(() -> service.updateMemberControlStatus(1L, 101L, null, true, null))
                .isInstanceOf(ApplicationException.class)
                .matches(ex -> ((ApplicationException) ex).getCode() == FamilyErrorCode.FAMILY_MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("데이터 한도가 가족 공유 데이터량을 초과하면 예외")
    void dataLimitExceedsFamilyAmount() {
        when(familyRepository.existsFamilyById(1L)).thenReturn(true);
        when(familySubRepository.existsFamilySub(1L, 101L)).thenReturn(true);
        when(familyRepository.findFamilyDataAmount(1L)).thenReturn(java.util.Optional.of(1048576L));

        assertThatThrownBy(() -> service.updateMemberControlStatus(1L, 101L, 2L, null, null))
                .isInstanceOf(ApplicationException.class)
                .matches(ex -> ((ApplicationException) ex).getCode()
                        == FamilyErrorCode.DATA_LIMIT_EXCEEDS_FAMILY_AMOUNT);
    }

    @Test
    @DisplayName("가족 존재 확인 이후 데이터량이 비어있으면 내부 예외")
    void familyDataAmountMissingAfterExistenceCheck() {
        when(familyRepository.existsFamilyById(1L)).thenReturn(true);
        when(familySubRepository.existsFamilySub(1L, 101L)).thenReturn(true);
        when(familyRepository.findFamilyDataAmount(1L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> service.updateMemberControlStatus(1L, 101L, 1L, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("familyId=1");
    }

    @Test
    @DisplayName("자녀를 부모로 변경 성공")
    void updateChildToParentSuccess() {
        when(familyRepository.existsFamilyById(1L)).thenReturn(true);
        when(familySubRepository.existsFamilySub(1L, 101L)).thenReturn(true);
        when(familySubRepository.findFamilyRole(1L, 101L)).thenReturn(java.util.Optional.of(FamilyRole.CHILD));
        when(familySubRepository.updateMemberFamilyRole(1L, 101L, FamilyRole.PARENT)).thenReturn(1);

        service.updateMemberControlStatus(1L, 101L, null, null, true);

        verify(familySubRepository).updateMemberFamilyRole(1L, 101L, FamilyRole.PARENT);
    }

    @Test
    @DisplayName("부모를 자녀로 변경 성공")
    void updateParentToChildSuccess() {
        when(familyRepository.existsFamilyById(1L)).thenReturn(true);
        when(familySubRepository.existsFamilySub(1L, 101L)).thenReturn(true);
        when(familySubRepository.findFamilyRole(1L, 101L)).thenReturn(java.util.Optional.of(FamilyRole.PARENT));
        when(familySubRepository.updateMemberFamilyRole(1L, 101L, FamilyRole.CHILD)).thenReturn(1);

        service.updateMemberControlStatus(1L, 101L, null, null, false);

        verify(familySubRepository).updateMemberFamilyRole(1L, 101L, FamilyRole.CHILD);
    }

    @Test
    @DisplayName("OWNER의 isParent 변경 시 예외")
    void updateOwnerRoleForbidden() {
        when(familyRepository.existsFamilyById(1L)).thenReturn(true);
        when(familySubRepository.existsFamilySub(1L, 101L)).thenReturn(true);
        when(familySubRepository.findFamilyRole(1L, 101L)).thenReturn(java.util.Optional.of(FamilyRole.OWNER));

        assertThatThrownBy(() -> service.updateMemberControlStatus(1L, 101L, null, null, true))
                .isInstanceOf(ApplicationException.class)
                .matches(ex -> ((ApplicationException) ex).getCode() == FamilyErrorCode.OWNER_ROLE_NOT_UPDATABLE);
    }
}
