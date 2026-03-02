package hotspot.admin.family.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.admin.family.domain.FamilyRole;
import hotspot.admin.family.infrastructure.entity.FamilyEntity;
import hotspot.admin.family.infrastructure.jpa.FamilyJpaRepository;
import hotspot.admin.family.infrastructure.jpa.FamilySubJpaRepository;
import hotspot.admin.family.infrastructure.jpa.FamilySubRepositoryImpl;
import hotspot.admin.subscription.infrastructure.SubscriptionJpaRepository;
import hotspot.admin.subscription.infrastructure.entity.SubscriptionEntity;

@ExtendWith(MockitoExtension.class)
class FamilySubRepositoryImplTest {

    @Mock
    private FamilySubJpaRepository familySubJpaRepository;

    @Mock
    private FamilyJpaRepository familyJpaRepository;

    @Mock
    private SubscriptionJpaRepository subscriptionJpaRepository;

    @Test
    @DisplayName("family_sub 존재 여부 조회 성공")
    void existsFamilySubSuccess() {
        FamilySubRepositoryImpl familySubRepository = new FamilySubRepositoryImpl(
                familySubJpaRepository,
                familyJpaRepository,
                subscriptionJpaRepository
        );
        when(familySubJpaRepository.existsByFamilyFamilyIdAndSubscriptionSubId(1L, 2L))
                .thenReturn(true);

        boolean exists = familySubRepository.existsFamilySub(1L, 2L);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("family_sub 최대 우선순위 조회 성공")
    void findMaxPrioritySuccess() {
        FamilySubRepositoryImpl familySubRepository = new FamilySubRepositoryImpl(
                familySubJpaRepository,
                familyJpaRepository,
                subscriptionJpaRepository
        );
        when(familySubJpaRepository.findMaxPriorityByFamilyId(1L)).thenReturn(7);

        int max = familySubRepository.findMaxPriority(1L);

        assertThat(max).isEqualTo(7);
    }

    @Test
    @DisplayName("family_sub 저장 성공")
    void saveFamilySubSuccess() {
        FamilySubRepositoryImpl familySubRepository = new FamilySubRepositoryImpl(
                familySubJpaRepository,
                familyJpaRepository,
                subscriptionJpaRepository
        );
        FamilyEntity family = FamilyEntity.builder().familyId(3L).build();
        SubscriptionEntity subscription = SubscriptionEntity.builder().subId(100L).build();
        when(familyJpaRepository.getReferenceById(3L)).thenReturn(family);
        when(subscriptionJpaRepository.getReferenceById(100L)).thenReturn(subscription);

        familySubRepository.saveFamilySub(3L, 100L, FamilyRole.CHILD, 2, 0L);

        verify(familySubJpaRepository).save(any());
    }

    @Test
    @DisplayName("활성 구성원 수 조회 성공")
    void countActiveMembersSuccess() {
        FamilySubRepositoryImpl familySubRepository = new FamilySubRepositoryImpl(
                familySubJpaRepository,
                familyJpaRepository,
                subscriptionJpaRepository
        );
        when(familySubJpaRepository.countActiveMembersByFamilyId(3L)).thenReturn(4);

        int count = familySubRepository.countActiveMembers(3L);

        assertThat(count).isEqualTo(4);
    }

    @Test
    @DisplayName("family_sub 데이터 한도 업데이트 성공")
    void updateDataLimitSuccess() {
        FamilySubRepositoryImpl familySubRepository = new FamilySubRepositoryImpl(
                familySubJpaRepository,
                familyJpaRepository,
                subscriptionJpaRepository
        );
        when(familySubJpaRepository.updateDataLimitByFamilyId(3L, 1024L)).thenReturn(3);

        int updated = familySubRepository.updateDataLimit(3L, 1024L);

        assertThat(updated).isEqualTo(3);
    }

    @Test
    @DisplayName("family_sub 우선순위 업데이트 성공")
    void updatePrioritySuccess() {
        FamilySubRepositoryImpl familySubRepository = new FamilySubRepositoryImpl(
                familySubJpaRepository,
                familyJpaRepository,
                subscriptionJpaRepository
        );
        when(familySubJpaRepository.updatePriorityByFamilyId(3L, -1)).thenReturn(3);

        int updated = familySubRepository.updatePriority(3L, -1);

        assertThat(updated).isEqualTo(3);
    }
}
