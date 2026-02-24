package hotspot.admin.appservice.domain;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BlockedServiceSub {
    private final Long blockedServiceSubId;
    private final Long subId;
    private final Long blockedServiceId;
    private final Boolean isDeleted;
    private final LocalDateTime createdTime;
    private final LocalDateTime modifiedTime;
}
