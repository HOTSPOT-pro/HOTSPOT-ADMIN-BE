package hotspot.admin.family.infrastructure.schema;

import java.util.List;

public record FamilyDataControl(
        Long familyDataLimit,
        List<SubFamilyDataControl> subFamilies
) {
    public record SubFamilyDataControl(
            Long subId,
            Long familyDataUsage
    ){}
}
