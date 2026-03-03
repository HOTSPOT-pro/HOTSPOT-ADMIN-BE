package hotspot.admin.family.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FamilyRemoveSchedule {
    private final Long familyRemoveScheduleId;
    private final Long targetSubId;
    private final Long familyId;
    private final DeleteStatus status;
    private final LocalDate scheduleDate;
    private final LocalDateTime createdTime;
    private final LocalDateTime modifiedTime;
}
