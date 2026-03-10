package hotspot.admin.family.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.security.GeneralSecurityException;
import java.util.List;

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
import hotspot.admin.family.service.port.FamilyQueryRepository;

@ExtendWith(MockitoExtension.class)
class GetFamilyListServiceTest {

    @Mock
    private FamilyQueryRepository familyQueryRepository;

    @Mock
    private PhoneCryptoUtil phoneCryptoUtil;

    private GetFamilyListServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GetFamilyListServiceImpl(familyQueryRepository, phoneCryptoUtil);
    }

    @Test
    @DisplayName("가족 목록 조회 성공 - 페이지 메타 계산")
    void getFamilyListSuccess() throws Exception {
        FamilyListRequest request = new FamilyListRequest();
        request.setPage(0);
        request.setSize(20);

        List<FamilyListItem> rows = List.of(
                FamilyListItem.builder()
                        .familyId(1L)
                        .subId(101L)
                        .representativeName("대표자1")
                        .phoneNumber("enc-1")
                        .memberCount(2)
                        .build(),
                FamilyListItem.builder()
                        .familyId(2L)
                        .subId(102L)
                        .representativeName("대표자2")
                        .phoneNumber("enc-2")
                        .memberCount(3)
                        .build()
        );

        when(familyQueryRepository.countFamilyList())
                .thenReturn(25L);
        when(familyQueryRepository.findFamilyList(20, 0))
                .thenReturn(rows);
        when(phoneCryptoUtil.decryptPhone(anyString(), anyLong()))
                .thenReturn("01012340000");

        FamilyListResponse response = service.getFamilyList(request);

        assertThat(response.page()).isEqualTo(0);
        assertThat(response.size()).isEqualTo(20);
        assertThat(response.totalElements()).isEqualTo(25L);
        assertThat(response.totalPages()).isEqualTo(2);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.familyList()).hasSize(2);
        assertThat(response.familyList().get(0).phoneNumber()).isEqualTo("010-****-0000");
    }

    @Test
    @DisplayName("전화번호 복호화 실패 시 예외")
    void decryptFailThenException() throws Exception {
        FamilyListRequest request = new FamilyListRequest();
        request.setPage(0);
        request.setSize(20);

        List<FamilyListItem> rows = List.of(FamilyListItem.builder()
                .familyId(1L)
                .subId(101L)
                .representativeName("대표자")
                .phoneNumber("enc")
                .memberCount(2)
                .build());

        when(familyQueryRepository.countFamilyList())
                .thenReturn(1L);
        when(familyQueryRepository.findFamilyList(20, 0))
                .thenReturn(rows);
        when(phoneCryptoUtil.decryptPhone("enc", 101L))
                .thenThrow(new GeneralSecurityException("decrypt failed"));

        assertThatThrownBy(() -> service.getFamilyList(request))
                .isInstanceOf(ApplicationException.class)
                .matches(ex -> ((ApplicationException) ex).getCode() == FamilyErrorCode.PHONE_DECRYPT_FAILED);
    }
}
