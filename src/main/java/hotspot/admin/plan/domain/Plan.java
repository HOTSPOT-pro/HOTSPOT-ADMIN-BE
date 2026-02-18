package hotspot.admin.plan.domain;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Plan {
    private final Long planId;
    private final String planName;
    private final Long planDataAmount;
    private final DataPeriod dataPeriod;
    private final Boolean isDeleted;
    private final LocalDateTime createdTime;
    private final LocalDateTime modifiedTime;
}
