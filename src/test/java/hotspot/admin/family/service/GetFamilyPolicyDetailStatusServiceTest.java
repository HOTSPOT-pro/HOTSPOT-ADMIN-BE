package hotspot.admin.family.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.security.GeneralSecurityException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.code.FamilyErrorCode;
import hotspot.admin.common.util.PhoneCryptoUtil;
import hotspot.admin.family.controller.response.FamilyPolicyAppItem;
import hotspot.admin.family.controller.response.FamilyPolicyMemberDetailItem;
import hotspot.admin.family.controller.response.FamilyPolicyTimeItem;
import hotspot.admin.family.domain.FamilyRole;
import hotspot.admin.family.infrastructure.query.dto.FamilyPolicyAppOptionRow;
import hotspot.admin.family.infrastructure.query.dto.FamilyPolicyAppPolicyRow;
import hotspot.admin.family.infrastructure.query.dto.FamilyPolicyMemberRow;
import hotspot.admin.family.infrastructure.query.dto.FamilyPolicyTimeOptionRow;
import hotspot.admin.family.infrastructure.query.dto.FamilyPolicyTimePolicyRow;
import hotspot.admin.family.service.port.FamilyRepository;
import hotspot.admin.family.service.port.FamilySubQueryRepository;
import hotspot.admin.policy.domain.PolicyType;

@ExtendWith(MockitoExtension.class)
class GetFamilyPolicyDetailStatusServiceTest {

    @Mock
    private FamilyRepository familyRepository;

    @Mock
    private FamilySubQueryRepository familySubQueryRepository;

    @Mock
    private PhoneCryptoUtil phoneCryptoUtil;

    private GetFamilyPolicyDetailStatusServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GetFamilyPolicyDetailStatusServiceImpl(
                familyRepository,
                familySubQueryRepository,
                phoneCryptoUtil,
                new ObjectMapper()
        );
    }

    @Test
    @DisplayName("가족 정책 상세 조회 성공")
    void getFamilyPolicyDetailStatusSuccess() throws Exception {
        when(familyRepository.existsFamilyById(1L)).thenReturn(true);
        when(familySubQueryRepository.findFamilyPolicyMembers(1L))
                .thenReturn(List.of(
                        FamilyPolicyMemberRow.builder()
                                .subId(10L)
                                .memberName("대표")
                                .phoneNumberEnc("enc-1")
                                .familyRole(FamilyRole.OWNER)
                                .blocked(true)
                                .build(),
                        FamilyPolicyMemberRow.builder()
                                .subId(11L)
                                .memberName("자녀")
                                .phoneNumberEnc("enc-2")
                                .familyRole(FamilyRole.CHILD)
                                .blocked(false)
                                .build()
                ));
        when(familySubQueryRepository.findFamilyTimePolicyOptions(1L))
                .thenReturn(List.of(
                        FamilyPolicyTimeOptionRow.builder()
                                .policyId(101L)
                                .policyName("야간 차단")
                                .policyDescription("매일 야간 차단")
                                .policyType(PolicyType.SCHEDULED)
                                .policySnapshotJson("""
                                        {"days":["MON","TUE","WED","THU","FRI"],"startTime":"22:00","endTime":"07:00"}
                                        """)
                                .build(),
                        FamilyPolicyTimeOptionRow.builder()
                                .policyId(102L)
                                .policyName("학습 시간")
                                .policyDescription("주말 학습 시간")
                                .policyType(PolicyType.SCHEDULED)
                                .policySnapshotJson("""
                                        {"days":["SAT","SUN"],"startTime":"10:00","endTime":"12:00"}
                                        """)
                                .build()
                ));
        when(familySubQueryRepository.findAllAppPolicyOptions())
                .thenReturn(List.of(
                        FamilyPolicyAppOptionRow.builder().policyId(201L).policyName("유튜브").build(),
                        FamilyPolicyAppOptionRow.builder().policyId(202L).policyName("틱톡").build()
                ));
        when(familySubQueryRepository.findFamilyTimePolicies(1L))
                .thenReturn(List.of(
                        FamilyPolicyTimePolicyRow.builder().subId(10L).policyId(101L).policyName("야간 차단").build(),
                        FamilyPolicyTimePolicyRow.builder().subId(11L).policyId(102L).policyName("학습 시간").build()
                ));
        when(familySubQueryRepository.findFamilyAppPolicies(1L))
                .thenReturn(List.of(
                        FamilyPolicyAppPolicyRow.builder().subId(10L).policyId(201L).blockedServiceName("유튜브").build(),
                        FamilyPolicyAppPolicyRow.builder().subId(10L).policyId(202L).blockedServiceName("틱톡").build()
                ));
        when(phoneCryptoUtil.decryptPhone("enc-1")).thenReturn("01011112222");

        FamilyPolicyMemberDetailItem response = service.getFamilyPolicyDetailStatus(1L, 10L);

        assertThat(response.memberName()).isEqualTo("대표");
        assertThat(response.phoneNumber()).isEqualTo("010-****-2222");
        assertThat(response.appliedTimePolicies()).containsExactly(
                FamilyPolicyTimeItem.builder()
                        .policyId(101L)
                        .policyName("야간 차단")
                        .policyDescription("매일 야간 차단")
                        .policyType(PolicyType.SCHEDULED)
                        .policyScheduleLabel("주중 22:00~07:00")
                        .isActive(true)
                        .build(),
                FamilyPolicyTimeItem.builder()
                        .policyId(102L)
                        .policyName("학습 시간")
                        .policyDescription("주말 학습 시간")
                        .policyType(PolicyType.SCHEDULED)
                        .policyScheduleLabel("주말 10:00~12:00")
                        .isActive(false)
                        .build()
        );
        assertThat(response.appliedBlockedServicePolicies()).containsExactly(
                FamilyPolicyAppItem.builder().policyId(201L).policyName("유튜브").isActive(true).build(),
                FamilyPolicyAppItem.builder().policyId(202L).policyName("틱톡").isActive(true).build()
        );
    }

    @Test
    @DisplayName("가족이 없으면 FAMILY_NOT_FOUND")
    void familyNotFound() {
        when(familyRepository.existsFamilyById(999L)).thenReturn(false);

        assertThatThrownBy(() -> service.getFamilyPolicyDetailStatus(999L, 1L))
                .isInstanceOf(ApplicationException.class)
                .matches(ex -> ((ApplicationException) ex).getCode() == FamilyErrorCode.FAMILY_NOT_FOUND);
    }

    @Test
    @DisplayName("가족 구성원이 없으면 FAMILY_MEMBER_NOT_FOUND")
    void familyMemberNotFound() {
        when(familyRepository.existsFamilyById(1L)).thenReturn(true);
        when(familySubQueryRepository.findFamilyPolicyMembers(1L))
                .thenReturn(List.of(FamilyPolicyMemberRow.builder()
                        .subId(10L)
                        .memberName("대표")
                        .phoneNumberEnc("enc-1")
                        .familyRole(FamilyRole.OWNER)
                        .blocked(true)
                        .build()));
        when(familySubQueryRepository.findFamilyTimePolicyOptions(1L)).thenReturn(List.of());
        when(familySubQueryRepository.findAllAppPolicyOptions()).thenReturn(List.of());
        when(familySubQueryRepository.findFamilyTimePolicies(1L)).thenReturn(List.of());
        when(familySubQueryRepository.findFamilyAppPolicies(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> service.getFamilyPolicyDetailStatus(1L, 999L))
                .isInstanceOf(ApplicationException.class)
                .matches(ex -> ((ApplicationException) ex).getCode() == FamilyErrorCode.FAMILY_MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("전화번호 복호화 실패 시 예외")
    void decryptFailThenException() throws Exception {
        when(familyRepository.existsFamilyById(1L)).thenReturn(true);
        when(familySubQueryRepository.findFamilyPolicyMembers(1L))
                .thenReturn(List.of(FamilyPolicyMemberRow.builder()
                        .subId(1L)
                        .memberName("대표")
                        .phoneNumberEnc("enc")
                        .familyRole(FamilyRole.OWNER)
                        .blocked(false)
                        .build()));
        when(familySubQueryRepository.findFamilyTimePolicyOptions(1L)).thenReturn(List.of());
        when(familySubQueryRepository.findAllAppPolicyOptions()).thenReturn(List.of());
        when(familySubQueryRepository.findFamilyTimePolicies(1L)).thenReturn(List.of());
        when(familySubQueryRepository.findFamilyAppPolicies(1L)).thenReturn(List.of());
        when(phoneCryptoUtil.decryptPhone("enc")).thenThrow(new GeneralSecurityException("decrypt failed"));

        assertThatThrownBy(() -> service.getFamilyPolicyDetailStatus(1L, 1L))
                .isInstanceOf(ApplicationException.class)
                .matches(ex -> ((ApplicationException) ex).getCode() == FamilyErrorCode.PHONE_DECRYPT_FAILED);
    }
}
