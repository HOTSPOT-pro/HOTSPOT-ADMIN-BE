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
import hotspot.admin.family.infrastructure.entity.FamilyApplyEntity;
import hotspot.admin.family.infrastructure.entity.FamilyEntity;
import hotspot.admin.family.infrastructure.jpa.FamilyApplyJpaRepository;
import hotspot.admin.family.infrastructure.jpa.FamilyApplyRepositoryImpl;
import hotspot.admin.family.service.dto.FamilyRequestOutboxInfo;
import hotspot.admin.member.infrastructure.entity.MemberEntity;
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
    @DisplayName("요청자 subId 조회 성공")
    void findRequesterSubIdSuccess() {
        FamilyApplyRepositoryImpl familyApplyRepository = new FamilyApplyRepositoryImpl(familyApplyJpaRepository);
        when(familyApplyJpaRepository.findRequesterSubIdByFamilyApplyIdAndApplyType(1L, ApplyType.ADD))
                .thenReturn(Optional.of(10L));

        Optional<Long> result = familyApplyRepository.findRequesterSubId(1L, ApplyType.ADD);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Outbox 생성용 요청 정보 조회 성공")
    void findFamilyRequestOutboxInfoSuccess() {
        FamilyApplyRepositoryImpl familyApplyRepository = new FamilyApplyRepositoryImpl(familyApplyJpaRepository);

        MemberEntity requesterMember = MemberEntity.builder()
                .memberId(1L)
                .name("requester")
                .birth("900101")
                .build();
        SubscriptionEntity requesterSubscription = SubscriptionEntity.builder()
                .subId(11L)
                .member(requesterMember)
                .build();
        FamilyEntity family = FamilyEntity.builder()
                .familyId(33L)
                .build();

        FamilyApplyEntity entity = FamilyApplyEntity.builder()
                .familyApplyId(44L)
                .requesterSubscription(requesterSubscription)
                .family(family)
                .applyType(ApplyType.ADD)
                .status(FamilyApplyStatus.PENDING)
                .build();

        when(familyApplyJpaRepository.findOutboxSourceByFamilyApplyIdAndApplyType(44L, ApplyType.ADD))
                .thenReturn(Optional.of(entity));
        when(familyApplyJpaRepository.findTargetNamesByFamilyApplyId(44L))
                .thenReturn("target-name");

        Optional<FamilyRequestOutboxInfo> result = familyApplyRepository.findFamilyRequestOutboxInfo(
                44L,
                ApplyType.ADD
        );

        assertThat(result).isPresent();
        assertThat(result.get().targetName()).isEqualTo("target-name");
        assertThat(result.get().familyApply().getFamilyApplyId()).isEqualTo(44L);
        assertThat(result.get().familyApply().getRequesterSubId()).isEqualTo(11L);
        assertThat(result.get().familyApply().getTargets()).isEmpty();
        assertThat(result.get().familyApply().getFamilyId()).isEqualTo(33L);
    }
}
