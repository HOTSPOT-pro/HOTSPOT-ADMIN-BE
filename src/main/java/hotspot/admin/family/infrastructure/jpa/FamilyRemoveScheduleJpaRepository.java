package hotspot.admin.family.infrastructure.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import hotspot.admin.family.domain.DeleteStatus;
import hotspot.admin.family.infrastructure.entity.FamilyRemoveScheduleEntity;

public interface FamilyRemoveScheduleJpaRepository extends JpaRepository<FamilyRemoveScheduleEntity, Long> {
    List<FamilyRemoveScheduleEntity> findAllByTargetSubIdInAndStatus(List<Long> targetSubIds, DeleteStatus status);
}
