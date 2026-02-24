package hotspot.admin.policy.domain;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PolicySub {
    private final Long policySubId;
    private final Long subId;
    private final Long policyId;
    private final DateSnapshot dateSnapshot;
    private final Boolean isDeleted;
    private final LocalDateTime createdTime;
    private final LocalDateTime modifiedTime;
}
