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
import hotspot.admin.common.util.PhoneHashUtil;
import hotspot.admin.family.controller.response.FamilyListItem;
import hotspot.admin.family.controller.response.FamilyPhoneSearchResponse;
import hotspot.admin.family.service.port.FamilyRepository;

@ExtendWith(MockitoExtension.class)
class SearchFamilyByPhoneServiceTest {

    @Mock
    private FamilyRepository familyRepository;

    @Mock
    private PhoneHashUtil phoneHashUtil;

    @Mock
    private PhoneCryptoUtil phoneCryptoUtil;

    private SearchFamilyByPhoneServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SearchFamilyByPhoneServiceImpl(familyRepository, phoneHashUtil, phoneCryptoUtil);
    }

    @Test
    @DisplayName("전화번호 검색 성공")
    void searchByPhoneSuccess() throws Exception {
        when(phoneHashUtil.hashPhone("010-1234-5678"))
                .thenReturn("hashed-phone");
        when(familyRepository.findFamilyByPhoneHash("hashed-phone"))
                .thenReturn(Optional.of(FamilyListItem.builder()
                        .familyId(10L)
                        .representativeName("대표자")
                        .phoneNumber("encrypted-phone")
                        .memberCount(3)
                        .build()));
        when(phoneCryptoUtil.decryptPhone("encrypted-phone"))
                .thenReturn("01012345678");

        FamilyPhoneSearchResponse response = service.searchByPhone("010-1234-5678");

        assertThat(response.family()).isNotNull();
        assertThat(response.family().familyId()).isEqualTo(10L);
        assertThat(response.family().phoneNumber()).isEqualTo("010-****-5678");
    }

    @Test
    @DisplayName("전화번호 형식이 잘못되면 예외")
    void invalidPhoneThenException() throws Exception {
        when(phoneHashUtil.hashPhone("invalid"))
                .thenThrow(new IllegalArgumentException("invalid"));

        assertThatThrownBy(() -> service.searchByPhone("invalid"))
                .isInstanceOf(ApplicationException.class)
                .matches(ex -> ((ApplicationException) ex).getCode() == FamilyErrorCode.INVALID_PHONE_NUMBER);
    }

    @Test
    @DisplayName("전화번호 해시 생성 실패 시 예외")
    void hashFailThenException() throws Exception {
        when(phoneHashUtil.hashPhone("010-1234-5678"))
                .thenThrow(new GeneralSecurityException("hash failed"));

        assertThatThrownBy(() -> service.searchByPhone("010-1234-5678"))
                .isInstanceOf(ApplicationException.class)
                .matches(ex -> ((ApplicationException) ex).getCode() == FamilyErrorCode.PHONE_HASH_FAILED);
    }
}
