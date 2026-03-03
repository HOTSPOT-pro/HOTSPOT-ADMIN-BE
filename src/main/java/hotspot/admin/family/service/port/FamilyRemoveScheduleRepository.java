package hotspot.admin.family.service.port;

import java.util.List;

import hotspot.admin.family.domain.DeleteStatus;
import hotspot.admin.family.domain.FamilyRemoveSchedule;

public interface FamilyRemoveScheduleRepository {
    List<FamilyRemoveSchedule> saveAll(List<FamilyRemoveSchedule> schedules);

    List<FamilyRemoveSchedule> findAllByTargetSubIdInAndStatus(List<Long> subIds, DeleteStatus status);
}
