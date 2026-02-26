package hotspot.admin.family.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.security.GeneralSecurityException;
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
import hotspot.admin.family.controller.response.FamilySummaryResponse;
import hotspot.admin.family.service.port.FamilyQueryRepository;

@ExtendWith(MockitoExtension.class)
class GetFamilySummaryServiceTest {

    @Mock
    private FamilyQueryRepository familyQueryRepository;

    @Mock
    private PhoneCryptoUtil phoneCryptoUtil;

    private GetFamilySummaryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GetFamilySummaryServiceImpl(familyQueryRepository, phoneCryptoUtil);
    }

    @Test
    @DisplayName("가족 상세 상단 조회 성공")
    void getFamilySummarySuccess() throws Exception {
        FamilyListItem row = FamilyListItem.builder()
                .familyId(3L)
                .representativeName("대표자")
                .phoneNumber("enc-phone")
                .memberCount(4)
                .build();

        when(familyQueryRepository.findFamilyById(3L))
                .thenReturn(Optional.of(row));
        when(phoneCryptoUtil.decryptPhone("enc-phone"))
                .thenReturn("01012345678");

        FamilySummaryResponse response = service.getFamilySummary(3L);

        assertThat(response.familyId()).isEqualTo(3L);
        assertThat(response.displayId()).isEqualTo("FAM-000003");
        assertThat(response.representativeName()).isEqualTo("대표자");
        assertThat(response.phoneNumber()).isEqualTo("010-****-5678");
        assertThat(response.memberCount()).isEqualTo(4);
    }

    @Test
    @DisplayName("가족이 없으면 FAMILY_NOT_FOUND")
    void familyNotFound() {
        when(familyQueryRepository.findFamilyById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getFamilySummary(999L))
                .isInstanceOf(ApplicationException.class)
                .matches(ex -> ((ApplicationException) ex).getCode() == FamilyErrorCode.FAMILY_NOT_FOUND);
    }

    @Test
    @DisplayName("전화번호 복호화 실패 시 예외")
    void decryptFailThenException() throws Exception {
        FamilyListItem row = FamilyListItem.builder()
                .familyId(8L)
                .representativeName("대표자")
                .phoneNumber("enc")
                .memberCount(2)
                .build();

        when(familyQueryRepository.findFamilyById(8L))
                .thenReturn(Optional.of(row));
        when(phoneCryptoUtil.decryptPhone("enc"))
                .thenThrow(new GeneralSecurityException("decrypt failed"));

        assertThatThrownBy(() -> service.getFamilySummary(8L))
                .isInstanceOf(ApplicationException.class)
                .matches(ex -> ((ApplicationException) ex).getCode() == FamilyErrorCode.PHONE_DECRYPT_FAILED);
    }
}
