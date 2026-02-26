package hotspot.admin.family.infrastructure.jpa;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import hotspot.admin.family.domain.PriorityType;
import hotspot.admin.family.infrastructure.entity.FamilyEntity;
import hotspot.admin.family.service.port.FamilyRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class FamilyRepositoryImpl implements FamilyRepository {

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

    /** 가족을 신규 생성하고 생성된 familyId를 반환한다. */
    @Override
    public Long createFamily(int familyNum, long familyDataAmount, PriorityType priorityType) {
        FamilyEntity saved = familyJpaRepository.save(FamilyEntity.builder()
                .familyNum(familyNum)
                .familyDataAmount(familyDataAmount)
                .priorityType(priorityType)
                .isDeleted(false)
                .build());
        return saved.getFamilyId();
    }

    /** 가족 구성원 수와 가족 공유 데이터량을 갱신한다. */
    @Override
    public int updateFamilySummary(Long familyId, int familyNum, long familyDataAmount) {
        return familyJpaRepository.updateFamilySummary(familyId, familyNum, familyDataAmount);
    }

    /** 가족 우선순위 유형(FIFO/PRIORITY)을 갱신한다. */
    @Override
    public int updateFamilyPriorityType(Long familyId, PriorityType priorityType) {
        return familyJpaRepository.updatePriorityTypeByFamilyId(familyId, priorityType);
    }
}
