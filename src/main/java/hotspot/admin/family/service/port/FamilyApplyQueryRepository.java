package hotspot.admin.family.service.port;

import java.util.List;

import hotspot.admin.family.domain.ApplyType;
import hotspot.admin.family.domain.FamilyApplyStatus;
import hotspot.admin.family.infrastructure.query.dto.FamilyApprovalTargetInfo;
import hotspot.admin.family.infrastructure.query.dto.FamilyRequestListRow;

public interface FamilyApplyQueryRepository {
    List<FamilyRequestListRow> findFamilyRequestList(
            ApplyType applyType,
            FamilyApplyStatus status,
            int limit,
            long offset
    );

    long countFamilyRequestList(ApplyType applyType, FamilyApplyStatus status);

    List<FamilyApprovalTargetInfo> findApprovalTargetInfos(Long familyApplyId, ApplyType applyType);
}
