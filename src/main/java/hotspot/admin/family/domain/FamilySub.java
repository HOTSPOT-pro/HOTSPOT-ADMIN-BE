package hotspot.admin.family.domain;

import hotspot.admin.subscription.domain.Subscription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FamilySub {
    private final Long familySubId;
    private Subscription subscription;
    private Family family;
    private final FamilyRole familyRole;
    private final Integer priority;
    private final Long dataLimit;
}
