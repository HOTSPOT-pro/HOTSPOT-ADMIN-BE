package hotspot.admin.family.service.port;

import java.util.List;
import java.util.Optional;

import hotspot.admin.family.controller.response.FamilyListItem;
import hotspot.admin.family.controller.response.FamilyRequestListItem;
import hotspot.admin.family.domain.ApplyType;
import hotspot.admin.family.domain.FamilyApplyStatus;
import hotspot.admin.family.domain.PriorityType;
import hotspot.admin.family.service.dto.FamilyControlMemberRow;
import hotspot.admin.family.service.dto.FamilyPolicyAppPolicyRow;
import hotspot.admin.family.service.dto.FamilyPolicyMemberRow;
import hotspot.admin.family.service.dto.FamilyPolicyStatusRow;
import hotspot.admin.family.service.dto.FamilyPolicyTimePolicyRow;

public interface FamilyRepository {
    List<FamilyListItem> findFamilyList(
            int limit,
            long offset
    );

    long countFamilyList();

    Optional<FamilyListItem> findFamilyByPhoneHash(String phoneHash);

    Optional<FamilyListItem> findFamilyById(Long familyId);

    boolean existsFamilyById(Long familyId);

    Optional<PriorityType> findFamilyPriorityType(Long familyId);

    List<FamilyControlMemberRow> findFamilyControlMembers(Long familyId);

    List<FamilyPolicyMemberRow> findFamilyPolicyMembers(Long familyId);

    List<FamilyPolicyTimePolicyRow> findFamilyTimePolicies(Long familyId);

    List<FamilyPolicyAppPolicyRow> findFamilyAppPolicies(Long familyId);

    List<FamilyPolicyStatusRow> findFamilyPolicyStatusRows(Long familyId);

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
