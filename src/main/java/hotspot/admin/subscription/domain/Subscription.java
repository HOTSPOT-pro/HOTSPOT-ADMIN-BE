package hotspot.admin.subscription.domain;

import java.time.LocalDateTime;

import hotspot.admin.member.domain.Member;
import hotspot.admin.plan.domain.Plan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Subscription {
    private final Long subId;
    private final Plan plan;
    private final Member member;
    private final String phoneEnc;
    private final String phoneHash;
    private final Integer phoneKeyBucketId;
    private final Integer phoneKeyVersion;
    private final Boolean isLocked;
    private final Boolean isDeleted;
    private final LocalDateTime createdTime;
    private final LocalDateTime modifiedTime;
}
