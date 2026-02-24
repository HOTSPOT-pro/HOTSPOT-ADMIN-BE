package hotspot.admin.policy.domain;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BlockPolicy {
    private final Long blockPolicyId;
    private final String policyName;
    private final String policyDescription;
    private final PolicyType policyType;
    private final PolicySnapshot policySnapshot;
    private final Boolean isActive;
    private final Boolean isDeleted;
    private final LocalDateTime createdTime;
    private final LocalDateTime modifiedTime;
}
