package hotspot.admin.family.infrastructure;

import org.springframework.stereotype.Repository;

import hotspot.admin.family.domain.ApplyType;
import hotspot.admin.family.domain.FamilyApplyStatus;
import hotspot.admin.family.service.port.FamilyApplyRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class FamilyApplyRepositoryImpl implements FamilyApplyRepository {

    private final FamilyApplyJpaRepository familyApplyJpaRepository;

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

    @Override
    public boolean existsFamilyRequest(Long familyApplyId, ApplyType applyType) {
        return familyApplyJpaRepository.existsByFamilyApplyIdAndApplyType(familyApplyId, applyType);
    }
}
