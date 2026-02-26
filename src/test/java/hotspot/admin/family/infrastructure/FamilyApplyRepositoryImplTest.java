package hotspot.admin.family.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.admin.family.domain.ApplyType;
import hotspot.admin.family.domain.FamilyApplyStatus;
import hotspot.admin.family.domain.FamilyRole;
import hotspot.admin.family.infrastructure.entity.FamilyApplyEntity;
import hotspot.admin.family.infrastructure.entity.FamilyEntity;
import hotspot.admin.family.infrastructure.jpa.FamilyApplyJpaRepository;
import hotspot.admin.family.infrastructure.jpa.FamilyApplyRepositoryImpl;
import hotspot.admin.family.service.dto.FamilyAddApprovalInfo;
import hotspot.admin.subscription.infrastructure.entity.SubscriptionEntity;

@ExtendWith(MockitoExtension.class)
class FamilyApplyRepositoryImplTest {

    @Mock
    private FamilyApplyJpaRepository familyApplyJpaRepository;

    @Test
    @DisplayName("요청 상태 업데이트 성공")
    void updateFamilyRequestStatusSuccess() {
        FamilyApplyRepositoryImpl familyApplyRepository = new FamilyApplyRepositoryImpl(familyApplyJpaRepository);
        when(familyApplyJpaRepository.updateStatusByIdAndTypeAndCurrentStatus(
                1L,
                ApplyType.ADD,
                FamilyApplyStatus.PENDING,
                FamilyApplyStatus.APPROVED
        )).thenReturn(1);

        int updated = familyApplyRepository.updateFamilyRequestStatus(
                1L,
                ApplyType.ADD,
                FamilyApplyStatus.PENDING,
                FamilyApplyStatus.APPROVED
        );

        assertThat(updated).isEqualTo(1);
    }

    @Test
    @DisplayName("요청 존재 여부 조회 성공")
    void existsFamilyRequestSuccess() {
        FamilyApplyRepositoryImpl familyApplyRepository = new FamilyApplyRepositoryImpl(familyApplyJpaRepository);
        when(familyApplyJpaRepository.existsByFamilyApplyIdAndApplyType(10L, ApplyType.REMOVE)).thenReturn(true);

        boolean exists = familyApplyRepository.existsFamilyRequest(10L, ApplyType.REMOVE);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("요청 승인 후처리 정보 조회 성공")
    void findAddApprovalInfoSuccess() {
        FamilyApplyRepositoryImpl familyApplyRepository = new FamilyApplyRepositoryImpl(familyApplyJpaRepository);
        FamilyEntity family = FamilyEntity.builder()
                .familyId(3L)
                .build();
        SubscriptionEntity targetSubscription = SubscriptionEntity.builder()
                .subId(10L)
                .build();
        FamilyApplyEntity entity = FamilyApplyEntity.builder()
                .familyApplyId(1L)
                .family(family)
                .targetSubscription(targetSubscription)
                .targetFamilyRole(FamilyRole.CHILD)
                .applyType(ApplyType.ADD)
                .build();

        when(familyApplyJpaRepository.findByFamilyApplyIdAndApplyType(1L, ApplyType.ADD))
                .thenReturn(Optional.of(entity));

        Optional<FamilyAddApprovalInfo> result = familyApplyRepository.findAddApprovalInfo(1L);

        assertThat(result).isPresent();
        assertThat(result.get().familyId()).isEqualTo(3L);
        assertThat(result.get().targetSubId()).isEqualTo(10L);
        assertThat(result.get().targetFamilyRole()).isEqualTo(FamilyRole.CHILD);
    }
}
