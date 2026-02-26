package hotspot.admin.family.infrastructure;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import hotspot.admin.family.domain.PriorityType;
import hotspot.admin.family.service.port.FamilyRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class FamilyRepositoryJpaImpl implements FamilyRepository {

    private final FamilyJpaRepository familyJpaRepository;

    /** 삭제되지 않은 가족 존재 여부를 확인한다. */
    @Override
    public boolean existsFamilyById(Long familyId) {
        return familyJpaRepository.existsByFamilyIdAndIsDeletedFalse(familyId);
    }

    /** 가족 우선순위 유형(FIFO/PRIORITY)을 조회한다. */
    @Override
    public Optional<PriorityType> findFamilyPriorityType(Long familyId) {
        return familyJpaRepository.findPriorityTypeByFamilyId(familyId);
    }
}
