package hotspot.admin.family.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.admin.family.domain.PriorityType;
import hotspot.admin.family.infrastructure.jpa.FamilyJpaRepository;
import hotspot.admin.family.infrastructure.jpa.FamilyRepositoryJpaImpl;

@ExtendWith(MockitoExtension.class)
class FamilyRepositoryJpaImplTest {

    @Mock
    private FamilyJpaRepository familyJpaRepository;

    @Test
    @DisplayName("가족 존재 여부 조회 성공")
    void existsFamilyByIdSuccess() {
        FamilyRepositoryJpaImpl familyRepository = new FamilyRepositoryJpaImpl(familyJpaRepository);
        when(familyJpaRepository.existsByFamilyIdAndIsDeletedFalse(1L)).thenReturn(true);

        boolean exists = familyRepository.existsFamilyById(1L);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("가족 우선순위 타입 조회 성공")
    void findFamilyPriorityTypeSuccess() {
        FamilyRepositoryJpaImpl familyRepository = new FamilyRepositoryJpaImpl(familyJpaRepository);
        when(familyJpaRepository.findPriorityTypeByFamilyId(1L)).thenReturn(Optional.of(PriorityType.FIFO));

        Optional<PriorityType> result = familyRepository.findFamilyPriorityType(1L);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(PriorityType.FIFO);
    }

    @Test
    @DisplayName("가족 요약 정보 업데이트 성공")
    void updateFamilySummarySuccess() {
        FamilyRepositoryJpaImpl familyRepository = new FamilyRepositoryJpaImpl(familyJpaRepository);
        when(familyJpaRepository.updateFamilySummary(1L, 4, 20971520L)).thenReturn(1);

        int updated = familyRepository.updateFamilySummary(1L, 4, 20971520L);

        assertThat(updated).isEqualTo(1);
    }
}
