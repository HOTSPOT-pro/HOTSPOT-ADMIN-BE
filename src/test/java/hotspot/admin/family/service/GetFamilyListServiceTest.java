package hotspot.admin.family.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.security.GeneralSecurityException;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.code.FamilyErrorCode;
import hotspot.admin.common.util.PhoneCryptoUtil;
import hotspot.admin.family.controller.request.FamilyListRequest;
import hotspot.admin.family.controller.response.FamilyListItem;
import hotspot.admin.family.controller.response.FamilyListResponse;
import hotspot.admin.family.service.port.FamilyRepository;

@ExtendWith(MockitoExtension.class)
class GetFamilyListServiceTest {

    @Mock
    private FamilyRepository familyRepository;

    @Mock
    private PhoneCryptoUtil phoneCryptoUtil;

    private GetFamilyListServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GetFamilyListServiceImpl(familyRepository, phoneCryptoUtil);
    }

    @Test
    @DisplayName("가족 목록 조회 성공 - hasNext true, nextCursor 계산")
    void getFamilyListSuccess() throws Exception {
        FamilyListRequest request = new FamilyListRequest();
        request.setSize(30);
        request.setCursor(0L);

        List<FamilyListItem> rows = IntStream.rangeClosed(1, 31)
                .mapToObj(i -> FamilyListItem.builder()
                        .familyId((long) i)
                        .representativeName("대표자" + i)
                        .phoneNumber("enc-" + i)
                        .memberCount(2)
                        .usedData(null)
                        .remainingData(null)
                        .build())
                .toList();

        when(familyRepository.findFamilySlice(31, 0L))
                .thenReturn(rows);
        when(phoneCryptoUtil.decryptPhone(anyString()))
                .thenReturn("01012340000");

        FamilyListResponse response = service.getFamilyList(request);

        assertThat(response.size()).isEqualTo(30);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.nextCursor()).isEqualTo(30L);
        assertThat(response.familyList()).hasSize(30);
        assertThat(response.familyList().get(0).phoneNumber()).isEqualTo("010-****-0000");
    }

    @Test
    @DisplayName("허용되지 않은 size는 예외")
    void invalidSizeThenException() {
        FamilyListRequest request = new FamilyListRequest();
        request.setSize(10);

        assertThatThrownBy(() -> service.getFamilyList(request))
                .isInstanceOf(ApplicationException.class)
                .matches(ex -> ((ApplicationException) ex).getCode() == FamilyErrorCode.INVALID_SIZE);
    }

    @Test
    @DisplayName("전화번호 복호화 실패 시 예외")
    void decryptFailThenException() throws Exception {
        FamilyListRequest request = new FamilyListRequest();
        request.setSize(30);
        request.setCursor(0L);

        List<FamilyListItem> rows = List.of(FamilyListItem.builder()
                .familyId(1L)
                .representativeName("대표자")
                .phoneNumber("enc")
                .memberCount(2)
                .usedData(null)
                .remainingData(null)
                .build());

        when(familyRepository.findFamilySlice(31, 0L))
                .thenReturn(rows);
        when(phoneCryptoUtil.decryptPhone("enc"))
                .thenThrow(new GeneralSecurityException("decrypt failed"));

        assertThatThrownBy(() -> service.getFamilyList(request))
                .isInstanceOf(ApplicationException.class)
                .matches(ex -> ((ApplicationException) ex).getCode() == FamilyErrorCode.PHONE_DECRYPT_FAILED);
    }
}
