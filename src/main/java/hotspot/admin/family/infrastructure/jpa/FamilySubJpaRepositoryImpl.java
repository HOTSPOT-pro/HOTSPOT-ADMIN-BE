package hotspot.admin.family.infrastructure.jpa;

import org.springframework.stereotype.Repository;

import hotspot.admin.family.domain.FamilyRole;
import hotspot.admin.family.infrastructure.entity.FamilySubEntity;
import hotspot.admin.family.service.port.FamilySubRepository;
import hotspot.admin.subscription.infrastructure.SubscriptionJpaRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class FamilySubJpaRepositoryImpl implements FamilySubRepository {

    private final FamilySubJpaRepository familySubJpaRepository;
    private final FamilyJpaRepository familyJpaRepository;
    private final SubscriptionJpaRepository subscriptionJpaRepository;

    /** familyId/subId 조합의 가족 구성원 존재 여부를 확인한다. */
    @Override
    public boolean existsFamilySub(Long familyId, Long subId) {
        return familySubJpaRepository.existsByFamilyFamilyIdAndSubscriptionSubId(familyId, subId);
    }

    /** PRIORITY 유형에서 다음 순번 계산을 위해 최대 우선순위를 조회한다. */
    @Override
    public int findMaxPriority(Long familyId) {
        return familySubJpaRepository.findMaxPriorityByFamilyId(familyId);
    }

    /** 가족 구성원을 family_sub에 추가한다. */
    @Override
    public void saveFamilySub(Long familyId, Long subId, FamilyRole familyRole, int priority, long dataLimit) {
        FamilySubEntity entity = FamilySubEntity.builder()
                .family(familyJpaRepository.getReferenceById(familyId))
                .subscription(subscriptionJpaRepository.getReferenceById(subId))
                .familyRole(familyRole)
                .priority(priority)
                .dataLimit(dataLimit)
                .build();
        familySubJpaRepository.save(entity);
    }

    /** 활성 구성원 수를 조회한다. */
    @Override
    public int countActiveMembers(Long familyId) {
        return familySubJpaRepository.countActiveMembersByFamilyId(familyId);
    }

    /** 가족 내 모든 구성원의 데이터 한도를 일괄 갱신한다. */
    @Override
    public int updateDataLimit(Long familyId, long dataLimit) {
        return familySubJpaRepository.updateDataLimitByFamilyId(familyId, dataLimit);
    }

    /** 가족 내 모든 구성원의 우선순위를 일괄 갱신한다. */
    @Override
    public int updatePriority(Long familyId, int priority) {
        return familySubJpaRepository.updatePriorityByFamilyId(familyId, priority);
    }
}
