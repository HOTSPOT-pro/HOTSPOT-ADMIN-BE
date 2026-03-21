package hotspot.admin.family.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.admin.family.domain.DeleteStatus;
import hotspot.admin.family.domain.FamilyRemoveSchedule;
import hotspot.admin.family.infrastructure.entity.FamilyRemoveScheduleEntity;
import hotspot.admin.family.infrastructure.jpa.FamilyRemoveScheduleJpaRepository;
import hotspot.admin.family.infrastructure.jpa.FamilyRemoveScheduleRepositoryImpl;

@ExtendWith(MockitoExtension.class)
class FamilyRemoveScheduleRepositoryImplTest {

    @Mock
    private FamilyRemoveScheduleJpaRepository familyRemoveScheduleJpaRepository;

    @Test
    @DisplayName("삭제 스케줄 저장 성공")
    void saveAllSuccess() {
        FamilyRemoveScheduleRepositoryImpl repository =
                new FamilyRemoveScheduleRepositoryImpl(familyRemoveScheduleJpaRepository);

        FamilyRemoveSchedule schedule = FamilyRemoveSchedule.builder()
                .familyRemoveScheduleId(null)
                .targetSubId(10L)
                .familyId(3L)
                .status(DeleteStatus.SCHEDULED)
                .scheduleDate(LocalDate.of(2026, 4, 1))
                .build();

        when(familyRemoveScheduleJpaRepository.saveAll(anyList()))
                .thenReturn(List.of(FamilyRemoveScheduleEntity.builder()
                        .familyRemoveScheduleId(1L)
                        .targetSubId(10L)
                        .familyId(3L)
                        .status(DeleteStatus.SCHEDULED)
                        .scheduleDate(LocalDate.of(2026, 4, 1))
                        .build()));

        List<FamilyRemoveSchedule> result = repository.saveAll(List.of(schedule));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFamilyRemoveScheduleId()).isEqualTo(1L);
        assertThat(result.get(0).getTargetSubId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("삭제 스케줄 조회 성공")
    void findAllByTargetSubIdInAndStatusSuccess() {
        FamilyRemoveScheduleRepositoryImpl repository =
                new FamilyRemoveScheduleRepositoryImpl(familyRemoveScheduleJpaRepository);

        when(familyRemoveScheduleJpaRepository.findAllByTargetSubIdInAndStatus(
                List.of(10L, 20L), DeleteStatus.SCHEDULED))
                .thenReturn(List.of(FamilyRemoveScheduleEntity.builder()
                        .familyRemoveScheduleId(1L)
                        .targetSubId(10L)
                        .familyId(3L)
                        .status(DeleteStatus.SCHEDULED)
                        .scheduleDate(LocalDate.of(2026, 4, 1))
                        .build()));

        List<FamilyRemoveSchedule> result =
                repository.findAllByTargetSubIdInAndStatus(List.of(10L, 20L), DeleteStatus.SCHEDULED);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFamilyId()).isEqualTo(3L);
    }
}
