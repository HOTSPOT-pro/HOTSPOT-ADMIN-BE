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
import hotspot.admin.family.infrastructure.entity.FamilyEntity;
import hotspot.admin.family.infrastructure.jpa.FamilyJpaRepository;
import hotspot.admin.family.infrastructure.jpa.FamilyRepositoryImpl;

@ExtendWith(MockitoExtension.class)
class FamilyRepositoryImplTest {

    @Mock
    private FamilyJpaRepository familyJpaRepository;

    @Test
    @DisplayName("가족 존재 여부 조회 성공")
    void existsFamilyByIdSuccess() {
        FamilyRepositoryImpl familyRepository = new FamilyRepositoryImpl(familyJpaRepository);
        when(familyJpaRepository.existsByFamilyIdAndIsDeletedFalse(1L)).thenReturn(true);

        boolean exists = familyRepository.existsFamilyById(1L);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("가족 우선순위 타입 조회 성공")
    void findFamilyPriorityTypeSuccess() {
        FamilyRepositoryImpl familyRepository = new FamilyRepositoryImpl(familyJpaRepository);
        when(familyJpaRepository.findPriorityTypeByFamilyId(1L)).thenReturn(Optional.of(PriorityType.FIFO));

        Optional<PriorityType> result = familyRepository.findFamilyPriorityType(1L);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(PriorityType.FIFO);
    }

    @Test
    @DisplayName("가족 생성 성공")
    void createFamilySuccess() {
        FamilyRepositoryImpl familyRepository = new FamilyRepositoryImpl(familyJpaRepository);
        when(familyJpaRepository.save(org.mockito.ArgumentMatchers.any(FamilyEntity.class)))
                .thenReturn(FamilyEntity.builder()
                        .familyId(55L)
                        .familyNum(3)
                        .familyDataAmount(15728640L)
                        .priorityType(PriorityType.FIFO)
                        .isDeleted(false)
                        .build());

        Long familyId = familyRepository.createFamily(3, 15728640L, PriorityType.FIFO);

        assertThat(familyId).isEqualTo(55L);
    }

    @Test
    @DisplayName("가족 요약 정보 업데이트 성공")
    void updateFamilySummarySuccess() {
        FamilyRepositoryImpl familyRepository = new FamilyRepositoryImpl(familyJpaRepository);
        when(familyJpaRepository.updateFamilySummary(1L, 4, 20971520L)).thenReturn(1);

        int updated = familyRepository.updateFamilySummary(1L, 4, 20971520L);

        assertThat(updated).isEqualTo(1);
    }

    @Test
    @DisplayName("가족 우선순위 유형 업데이트 성공")
    void updateFamilyPriorityTypeSuccess() {
        FamilyRepositoryJpaImpl familyRepository = new FamilyRepositoryJpaImpl(familyJpaRepository);
        when(familyJpaRepository.updatePriorityTypeByFamilyId(1L, PriorityType.PRIORITY)).thenReturn(1);

        int updated = familyRepository.updateFamilyPriorityType(1L, PriorityType.PRIORITY);

        assertThat(updated).isEqualTo(1);
    }
}
