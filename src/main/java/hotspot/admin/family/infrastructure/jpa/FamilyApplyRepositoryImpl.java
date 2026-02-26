package hotspot.admin.family.infrastructure.jpa;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import hotspot.admin.family.domain.ApplyType;
import hotspot.admin.family.domain.FamilyApplyStatus;
import hotspot.admin.family.infrastructure.entity.FamilyApplyEntity;
import hotspot.admin.family.service.dto.FamilyAddApprovalInfo;
import hotspot.admin.family.service.port.FamilyApplyRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class FamilyApplyRepositoryImpl implements FamilyApplyRepository {

    private final FamilyApplyJpaRepository familyApplyJpaRepository;

    /** 대기중 요청을 목표 상태(승인/반려)로 조건부 업데이트한다. */
    @Override
    public int updateFamilyRequestStatus(
            Long familyApplyId,
            ApplyType applyType,
            FamilyApplyStatus currentStatus,
            FamilyApplyStatus newStatus
    ) {
        return familyApplyJpaRepository.updateStatusByIdAndTypeAndCurrentStatus(
                familyApplyId,
                applyType,
                currentStatus,
                newStatus
        );
    }

    /** 요청 ID와 요청 유형으로 요청 존재 여부를 확인한다. */
    @Override
    public boolean existsFamilyRequest(Long familyApplyId, ApplyType applyType) {
        return familyApplyJpaRepository.existsByFamilyApplyIdAndApplyType(familyApplyId, applyType);
    }

    /** ADD 요청의 승인 후처리에 필요한 최소 정보를 조회한다. */
    @Override
    public Optional<FamilyAddApprovalInfo> findAddApprovalInfo(Long familyApplyId) {
        return familyApplyJpaRepository.findByFamilyApplyIdAndApplyType(familyApplyId, ApplyType.ADD)
                .map(this::toAddApprovalInfo);
    }

    /** family_apply 엔티티를 승인 후처리용 정보 DTO로 변환한다. */
    private FamilyAddApprovalInfo toAddApprovalInfo(FamilyApplyEntity entity) {
        return FamilyAddApprovalInfo.builder()
                .familyId(entity.getFamily().getFamilyId())
                .targetSubId(entity.getTargetSubscription().getSubId())
                .targetFamilyRole(entity.getTargetFamilyRole())
                .build();
    }
}
