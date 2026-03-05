package hotspot.admin.family.infrastructure.jpa;

import java.util.List;
import java.util.Optional;

import hotspot.admin.family.domain.FamilySub;
import org.springframework.stereotype.Repository;

import hotspot.admin.family.domain.FamilyRole;
import hotspot.admin.family.infrastructure.entity.FamilySubEntity;
import hotspot.admin.family.service.port.FamilySubRepository;
import hotspot.admin.subscription.infrastructure.SubscriptionJpaRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class FamilySubRepositoryImpl implements FamilySubRepository {

    private final FamilySubJpaRepository familySubJpaRepository;
    private final FamilyJpaRepository familyJpaRepository;
    private final SubscriptionJpaRepository subscriptionJpaRepository;

    @Override
    public List<FamilySub> findByFamilyId(Long familyId) {
        return familySubJpaRepository.findByFamilyFamilyId(familyId).stream()
                .map(FamilySubEntity::entityToDomain)
                .toList();
    }

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

    /** 특정 구성원의 우선순위를 갱신한다. */
    @Override
    public int updateMemberPriority(Long familyId, Long subId, int priority) {
        return familySubJpaRepository.updatePriorityByFamilyIdAndSubId(familyId, subId, priority);
    }

    /** 특정 구성원의 데이터 한도(KB)를 갱신한다. */
    @Override
    public int updateMemberDataLimit(Long familyId, Long subId, long dataLimit) {
        return familySubJpaRepository.updateDataLimitByFamilyIdAndSubId(familyId, subId, dataLimit);
    }

    /** 특정 구성원의 잠금 상태를 갱신한다. */
    @Override
    public int updateMemberBlocked(Long familyId, Long subId, boolean isBlocked) {
        return subscriptionJpaRepository.updateIsLockedByFamilyIdAndSubId(familyId, subId, isBlocked);
    }

    /** 특정 구성원의 가족 역할(OWNER/PARENT/CHILD)을 조회한다. */
    @Override
    public Optional<FamilyRole> findFamilyRole(Long familyId, Long subId) {
        return familySubJpaRepository.findFamilyRoleByFamilyIdAndSubId(familyId, subId);
    }

    /** 특정 구성원의 가족 역할(OWNER/PARENT/CHILD)을 갱신한다. */
    @Override
    public int updateMemberFamilyRole(Long familyId, Long subId, FamilyRole familyRole) {
        return familySubJpaRepository.updateFamilyRoleByFamilyIdAndSubId(familyId, subId, familyRole);
    }
}
