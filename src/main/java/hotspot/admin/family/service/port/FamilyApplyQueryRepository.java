package hotspot.admin.family.service.port;

import java.util.List;

import hotspot.admin.family.controller.response.FamilyRequestListItem;
import hotspot.admin.family.domain.ApplyType;
import hotspot.admin.family.domain.FamilyApplyStatus;

public interface FamilyApplyQueryRepository {
    List<FamilyRequestListItem> findFamilyRequestList(
            ApplyType applyType,
            FamilyApplyStatus status,
            int limit,
            long offset
    );

    long countFamilyRequestList(ApplyType applyType, FamilyApplyStatus status);
}
