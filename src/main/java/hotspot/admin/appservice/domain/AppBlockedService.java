package hotspot.admin.appservice.domain;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AppBlockedService {
    private final Long appBlockedServiceId;
    private final String blockedServiceName;
    private final String blockedServiceCode;
    private final Boolean isDeleted;
    private final LocalDateTime createdTime;
    private final LocalDateTime modifiedTime;
}
