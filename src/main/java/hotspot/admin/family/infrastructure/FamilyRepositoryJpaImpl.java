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

    @Override
    public boolean existsFamilyById(Long familyId) {
        return familyJpaRepository.existsByFamilyIdAndIsDeletedFalse(familyId);
    }

    @Override
    public Optional<PriorityType> findFamilyPriorityType(Long familyId) {
        return familyJpaRepository.findPriorityTypeByFamilyId(familyId);
    }
}
