package hotspot.admin.family.service.port;

import java.util.List;
import java.util.Optional;

import hotspot.admin.family.controller.response.FamilyListItem;
import hotspot.admin.family.controller.response.FamilyRequestListItem;
import hotspot.admin.family.domain.ApplyType;
import hotspot.admin.family.domain.FamilyApplyStatus;

public interface FamilyRepository {
    List<FamilyListItem> findFamilySlice(
            int limitPlusOne,
            Long cursorFamilyId
    );

    Optional<FamilyListItem> findFamilyByPhoneHash(String phoneHash);

    List<FamilyRequestListItem> findFamilyRequestList(
            ApplyType applyType,
            FamilyApplyStatus status,
            int limit,
            long offset
    );

    long countFamilyRequestList(ApplyType applyType, FamilyApplyStatus status);

    int updateFamilyRequestStatus(
            Long familyApplyId,
            ApplyType applyType,
            FamilyApplyStatus currentStatus,
            FamilyApplyStatus newStatus
    );

    boolean existsFamilyRequest(Long familyApplyId, ApplyType applyType);
}
