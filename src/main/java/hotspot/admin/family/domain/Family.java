package hotspot.admin.family.domain;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Family {
    private final Long familyId;
    private final Integer familyNum;
    private final Long familyDataAmount;
    private final PriorityType priorityType;
    private final Boolean isDeleted;
    private final LocalDateTime createdTime;
    private final LocalDateTime modifiedTime;
}
