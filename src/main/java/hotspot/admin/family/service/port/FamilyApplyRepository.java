package hotspot.admin.family.service.port;

import hotspot.admin.family.domain.ApplyType;
import hotspot.admin.family.domain.FamilyApplyStatus;

public interface FamilyApplyRepository {
    int updateFamilyRequestStatus(
            Long familyApplyId,
            ApplyType applyType,
            FamilyApplyStatus currentStatus,
            FamilyApplyStatus newStatus
    );

    boolean existsFamilyRequest(Long familyApplyId, ApplyType applyType);
}
