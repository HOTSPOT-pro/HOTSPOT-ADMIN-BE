package hotspot.admin.family.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.code.FamilyErrorCode;
import hotspot.admin.common.exception.code.PolicyErrorCode;
import hotspot.admin.family.controller.request.PolicyActiveRequest;
import hotspot.admin.family.service.port.FamilyPolicyAssignmentRepository;
import hotspot.admin.family.service.port.FamilyRepository;
import hotspot.admin.family.service.port.FamilySubRepository;
import hotspot.admin.outbox.consistencyOutbox.util.PolicyBlockSnapshotPublisher;
import hotspot.admin.outbox.consistencyOutbox.domain.event.subscription.app.AppBlockListUpdateEvent;

@ExtendWith(MockitoExtension.class)
class UpdateFamilyMemberPolicyStatusServiceTest {

    @Mock
    private FamilyRepository familyRepository;

    @Mock
    private FamilySubRepository familySubRepository;

    @Mock
    private FamilyPolicyAssignmentRepository familyPolicyAssignmentRepository;

    @Mock
    private PolicyBlockSnapshotPublisher policyBlockSnapshotPublisher;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private UpdateFamilyMemberPolicyStatusServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UpdateFamilyMemberPolicyStatusServiceImpl(
                familyRepository,
                familySubRepository,
                familyPolicyAssignmentRepository,
                policyBlockSnapshotPublisher,
                applicationEventPublisher
        );
    }

    @Test
    @DisplayName("가족 구성원 시간 정책 적용 상태 수정 성공")
    void updateMemberTimePolicyStatusSuccess() {

        List<PolicyActiveRequest> policies = List.of(
                new PolicyActiveRequest(101L, true),
                new PolicyActiveRequest(102L, false)
        );

        when(familyRepository.existsFamilyById(1L)).thenReturn(true);
        when(familySubRepository.existsFamilySub(1L, 10L)).thenReturn(true);

        when(familyPolicyAssignmentRepository.findExistingTimePolicyIds(1L, Set.of(101L, 102L)))
                .thenReturn(Set.of(101L, 102L));

        when(familyPolicyAssignmentRepository.updateMemberTimePolicyActive(10L, 101L, true))
                .thenReturn(0);

        when(familyPolicyAssignmentRepository.updateMemberTimePolicyActive(10L, 102L, false))
                .thenReturn(1);

        service.updateMemberTimePolicyStatus(1L, 10L, policies);

        verify(familyPolicyAssignmentRepository)
                .insertMemberTimePolicy(10L, 101L, true);

        verify(policyBlockSnapshotPublisher)
                .publish(10L, List.of(101L));
    }

    @Test
    @DisplayName("가족 구성원 앱 정책 적용 상태 수정 성공")
    void updateMemberAppPolicyStatusSuccess() {

        List<PolicyActiveRequest> policies = List.of(
                new PolicyActiveRequest(201L, true),
                new PolicyActiveRequest(202L, false)
        );

        when(familyRepository.existsFamilyById(1L)).thenReturn(true);
        when(familySubRepository.existsFamilySub(1L, 10L)).thenReturn(true);

        when(familyPolicyAssignmentRepository.findExistingAppPolicyIds(Set.of(201L, 202L)))
                .thenReturn(Set.of(201L, 202L));

        when(familyPolicyAssignmentRepository.updateMemberAppPolicyActive(10L, 201L, true))
                .thenReturn(1);

        when(familyPolicyAssignmentRepository.updateMemberAppPolicyActive(10L, 202L, false))
                .thenReturn(1);

        service.updateMemberAppPolicyStatus(1L, 10L, policies);

        verify(familyPolicyAssignmentRepository, never())
                .insertMemberAppPolicy(10L, 201L);

        verify(applicationEventPublisher)
                .publishEvent(org.mockito.ArgumentMatchers.any(AppBlockListUpdateEvent.class));
    }

    @Test
    @DisplayName("시간 정책 수정 시 가족이 없으면 FAMILY_NOT_FOUND")
    void familyNotFoundForTimePolicy() {
        when(familyRepository.existsFamilyById(999L)).thenReturn(false);

        assertThatThrownBy(() -> service.updateMemberTimePolicyStatus(
                999L,
                10L,
                List.of(new PolicyActiveRequest(101L, true))
        ))
                .isInstanceOf(ApplicationException.class)
                .matches(ex -> ((ApplicationException) ex).getCode() == FamilyErrorCode.FAMILY_NOT_FOUND);
    }

    @Test
    @DisplayName("앱 정책 수정 시 가족 구성원이 없으면 FAMILY_MEMBER_NOT_FOUND")
    void familyMemberNotFoundForAppPolicy() {
        when(familyRepository.existsFamilyById(1L)).thenReturn(true);
        when(familySubRepository.existsFamilySub(1L, 999L)).thenReturn(false);

        assertThatThrownBy(() -> service.updateMemberAppPolicyStatus(
                1L,
                999L,
                List.of(new PolicyActiveRequest(201L, true))
        ))
                .isInstanceOf(ApplicationException.class)
                .matches(ex -> ((ApplicationException) ex).getCode() == FamilyErrorCode.FAMILY_MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("가족 시간 정책이 아니면 POLICY_NOT_FOUND")
    void invalidTimePolicyThenPolicyNotFound() {
        when(familyRepository.existsFamilyById(1L)).thenReturn(true);
        when(familySubRepository.existsFamilySub(1L, 10L)).thenReturn(true);
        when(familyPolicyAssignmentRepository.findExistingTimePolicyIds(1L, Set.of(101L, 999L)))
                .thenReturn(Set.of(101L));

        assertThatThrownBy(() -> service.updateMemberTimePolicyStatus(
                1L,
                10L,
                List.of(
                        new PolicyActiveRequest(101L, true),
                        new PolicyActiveRequest(999L, false)
                )
        ))
                .isInstanceOf(ApplicationException.class)
                .matches(ex -> ((ApplicationException) ex).getCode() == PolicyErrorCode.POLICY_NOT_FOUND);
    }

    @Test
    @DisplayName("앱 정책이 없으면 POLICY_NOT_FOUND")
    void invalidAppPolicyThenPolicyNotFound() {
        when(familyRepository.existsFamilyById(1L)).thenReturn(true);
        when(familySubRepository.existsFamilySub(1L, 10L)).thenReturn(true);
        when(familyPolicyAssignmentRepository.findExistingAppPolicyIds(Set.of(777L))).thenReturn(Set.of());

        assertThatThrownBy(() -> service.updateMemberAppPolicyStatus(
                1L,
                10L,
                List.of(new PolicyActiveRequest(777L, true))
        ))
                .isInstanceOf(ApplicationException.class)
                .matches(ex -> ((ApplicationException) ex).getCode() == PolicyErrorCode.POLICY_NOT_FOUND);
    }
}
