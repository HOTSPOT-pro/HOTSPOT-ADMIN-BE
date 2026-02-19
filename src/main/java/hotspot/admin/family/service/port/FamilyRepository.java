package hotspot.admin.family.service.port;

import java.util.List;

import hotspot.admin.family.controller.response.FamilyListItem;

public interface FamilyRepository {
    List<FamilyListItem> findFamilySlice(
            int limitPlusOne,
            Long cursorFamilyId
    );
}
