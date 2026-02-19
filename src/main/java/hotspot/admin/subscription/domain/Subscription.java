package hotspot.admin.subscription.domain;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Subscription {
    private final Long subId;
    private final Long planId;
    private final Long memberId;
    private final String phoneEnc;
    private final String phoneHash;
    private final Boolean isLocked;
    private final Boolean isDeleted;
    private final LocalDateTime createdTime;
    private final LocalDateTime modifiedTime;
}
