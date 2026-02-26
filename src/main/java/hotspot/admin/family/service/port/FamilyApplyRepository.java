package hotspot.admin.family.service.port;

import java.util.Optional;

import hotspot.admin.family.domain.ApplyType;
import hotspot.admin.family.domain.FamilyApplyStatus;
import hotspot.admin.family.service.dto.FamilyAddApprovalInfo;

public interface FamilyApplyRepository {
    int updateFamilyRequestStatus(
            Long familyApplyId,
            ApplyType applyType,
            FamilyApplyStatus currentStatus,
            FamilyApplyStatus newStatus
    );

    boolean existsFamilyRequest(Long familyApplyId, ApplyType applyType);

    Optional<FamilyAddApprovalInfo> findAddApprovalInfo(Long familyApplyId);
}
