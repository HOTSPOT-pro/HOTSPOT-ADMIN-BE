package hotspot.admin.family.infrastructure.jpa;

import java.util.List;

import org.springframework.stereotype.Repository;

import hotspot.admin.family.domain.DeleteStatus;
import hotspot.admin.family.domain.FamilyRemoveSchedule;
import hotspot.admin.family.infrastructure.entity.FamilyRemoveScheduleEntity;
import hotspot.admin.family.service.port.FamilyRemoveScheduleRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class FamilyRemoveScheduleRepositoryImpl implements FamilyRemoveScheduleRepository {

    private final FamilyRemoveScheduleJpaRepository familyRemoveScheduleJpaRepository;

    @Override
    public List<FamilyRemoveSchedule> saveAll(List<FamilyRemoveSchedule> schedules) {
        List<FamilyRemoveScheduleEntity> entities = schedules.stream()
                .map(FamilyRemoveScheduleEntity::domainToEntity)
                .toList();

        return familyRemoveScheduleJpaRepository.saveAll(entities).stream()
                .map(FamilyRemoveScheduleEntity::entityToDomain)
                .toList();
    }

    @Override
    public List<FamilyRemoveSchedule> findAllByTargetSubIdInAndStatus(List<Long> subIds, DeleteStatus status) {
        return familyRemoveScheduleJpaRepository.findAllByTargetSubIdInAndStatus(subIds, status).stream()
                .map(FamilyRemoveScheduleEntity::entityToDomain)
                .toList();
    }
}
