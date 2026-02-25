package hotspot.admin.family.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.code.FamilyErrorCode;
import hotspot.admin.common.util.PhoneCryptoUtil;
import hotspot.admin.family.controller.response.FamilyListItem;
import hotspot.admin.family.controller.response.FamilyPolicyMemberStatusItem;
import hotspot.admin.family.domain.FamilyRole;
import hotspot.admin.family.service.dto.FamilyPolicyStatusRow;
import hotspot.admin.family.service.port.FamilyRepository;

@ExtendWith(MockitoExtension.class)
class GetFamilyPolicyStatusServiceTest {

    @Mock
    private FamilyRepository familyRepository;

    @Mock
    private PhoneCryptoUtil phoneCryptoUtil;

    private GetFamilyPolicyStatusServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GetFamilyPolicyStatusServiceImpl(familyRepository, phoneCryptoUtil);
    }

    @Test
    @DisplayName("가족 정책 적용 현황 조회 성공")
    void getFamilyPolicyStatusSuccess() throws Exception {
        when(familyRepository.findFamilyById(1L))
                .thenReturn(Optional.of(FamilyListItem.builder().familyId(1L).build()));
        when(familyRepository.findFamilyPolicyStatusRows(1L))
                .thenReturn(List.of(
                        FamilyPolicyStatusRow.builder()
                                .subId(10L)
                                .memberName("대표")
                                .phoneNumberEnc("enc-1")
                                .familyRole(FamilyRole.OWNER)
                                .blocked(true)
                                .appliedTimePolicies(List.of("야간 차단"))
                                .appliedBlockedServicePolicies(List.of("유튜브", "틱톡"))
                                .build(),
                        FamilyPolicyStatusRow.builder()
                                .subId(11L)
                                .memberName("자녀")
                                .phoneNumberEnc("enc-2")
                                .familyRole(FamilyRole.CHILD)
                                .blocked(false)
                                .appliedTimePolicies(List.of("학습 시간"))
                                .appliedBlockedServicePolicies(List.of())
                                .build()
                ));
        when(phoneCryptoUtil.decryptPhone("enc-1")).thenReturn("01011112222");
        when(phoneCryptoUtil.decryptPhone("enc-2")).thenReturn("01033334444");

        List<FamilyPolicyMemberStatusItem> response = service.getFamilyPolicyStatus(1L);

        assertThat(response).hasSize(2);
        assertThat(response.get(0).memberName()).isEqualTo("대표");
        assertThat(response.get(0).phoneNumber()).isEqualTo("010-****-2222");
        assertThat(response.get(0).appliedTimePolicies()).containsExactly("야간 차단");
        assertThat(response.get(0).appliedBlockedServicePolicies()).containsExactly("유튜브", "틱톡");
        assertThat(response.get(1).appliedBlockedServicePolicies()).isEmpty();
    }

    @Test
    @DisplayName("가족이 없으면 FAMILY_NOT_FOUND")
    void familyNotFound() {
        when(familyRepository.findFamilyById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getFamilyPolicyStatus(999L))
                .isInstanceOf(ApplicationException.class)
                .matches(ex -> ((ApplicationException) ex).getCode() == FamilyErrorCode.FAMILY_NOT_FOUND);
    }

    @Test
    @DisplayName("전화번호 복호화 실패 시 예외")
    void decryptFailThenException() throws Exception {
        when(familyRepository.findFamilyById(1L))
                .thenReturn(Optional.of(FamilyListItem.builder().familyId(1L).build()));
        when(familyRepository.findFamilyPolicyStatusRows(1L))
                .thenReturn(List.of(FamilyPolicyStatusRow.builder()
                        .subId(1L)
                        .memberName("대표")
                        .phoneNumberEnc("enc")
                        .familyRole(FamilyRole.OWNER)
                        .blocked(false)
                        .appliedTimePolicies(List.of())
                        .appliedBlockedServicePolicies(List.of())
                        .build()));
        when(phoneCryptoUtil.decryptPhone("enc")).thenThrow(new GeneralSecurityException("decrypt failed"));

        assertThatThrownBy(() -> service.getFamilyPolicyStatus(1L))
                .isInstanceOf(ApplicationException.class)
                .matches(ex -> ((ApplicationException) ex).getCode() == FamilyErrorCode.PHONE_DECRYPT_FAILED);
    }
}
