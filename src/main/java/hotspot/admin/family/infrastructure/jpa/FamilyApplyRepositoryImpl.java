package hotspot.admin.family.infrastructure.jpa;

import java.util.Collections;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import hotspot.admin.family.domain.ApplyType;
import hotspot.admin.family.domain.FamilyApply;
import hotspot.admin.family.domain.FamilyApplyStatus;
import hotspot.admin.family.infrastructure.entity.FamilyApplyEntity;
import hotspot.admin.family.service.dto.FamilyRequestOutboxInfo;
import hotspot.admin.family.service.port.FamilyApplyRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class FamilyApplyRepositoryImpl implements FamilyApplyRepository {

    private final FamilyApplyJpaRepository familyApplyJpaRepository;

    /** 대기중 가족 요청만 목표 상태(승인/반려)로 조건부 업데이트한다. */
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

    /** 요청 ID와 요청 유형으로 Outbox 생성에 필요한 정보를 조회한다. */
    @Override
    public Optional<FamilyRequestOutboxInfo> findFamilyRequestOutboxInfo(Long familyApplyId, ApplyType applyType) {
        return familyApplyJpaRepository.findOutboxSourceByFamilyApplyIdAndApplyType(familyApplyId, applyType)
                .map(this::toFamilyRequestOutboxInfo);
    }

    private FamilyRequestOutboxInfo toFamilyRequestOutboxInfo(FamilyApplyEntity entity) {
        // FamilyApply 구조 리팩토링에 의한 임시 수정
        String targetNames = familyApplyJpaRepository.findTargetNamesByFamilyApplyId(entity.getFamilyApplyId());
        if (targetNames == null || targetNames.isBlank()) {
            targetNames = "-";
        }
        FamilyApply familyApply = FamilyApply.builder()
                .familyApplyId(entity.getFamilyApplyId())
                .requesterSubId(entity.getRequesterSubscription().getSubId())
                .targetSubId(null)
                .familyId(entity.getFamily() == null ? null : entity.getFamily().getFamilyId())
                .applyType(entity.getApplyType())
                .targetFamilyRole(null)
                .docUrl(entity.getDocUrl())
                .status(entity.getStatus())
                .targets(Collections.emptyList())
                .createdTime(entity.getCreatedTime())
                .modifiedTime(entity.getModifiedTime())
                .build();

        return new FamilyRequestOutboxInfo(
                familyApply,
                targetNames
        );

//        return new FamilyRequestOutboxInfo(
//                entity.entityToDomain(),
//                entity.getTargetSubscription().getMember().getName()
//        );
    }
}
