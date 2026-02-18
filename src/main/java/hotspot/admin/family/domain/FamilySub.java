package hotspot.admin.family.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FamilySub {
    private final Long familySubId;
    private final Long subId;
    private final Long familyId;
    private final FamilyRole familyRole;
    private final Integer priority;
    private final Long dataLimit;
}
