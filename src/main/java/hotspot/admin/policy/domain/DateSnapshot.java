package hotspot.admin.policy.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DateSnapshot {

    private String policyName;

    private PolicyType policyType;

    private PolicySnapshot data;
}
