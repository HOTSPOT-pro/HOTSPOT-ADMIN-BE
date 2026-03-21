package hotspot.admin.family.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FamilyApplyTarget {
    private final Long familyApplyTargetId;
    private final Long familyApplyId;
    private final Long targetSubId;
    private final FamilyRole targetFamilyRole;
}
