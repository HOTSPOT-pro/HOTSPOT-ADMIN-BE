package hotspot.admin.family.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
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
import hotspot.admin.family.controller.request.FamilyRequestListRequest;
import hotspot.admin.family.controller.response.FamilyRequestListItem;
import hotspot.admin.family.controller.response.FamilyRequestListResponse;
import hotspot.admin.family.controller.response.FamilyRequestTargetItem;
import hotspot.admin.family.domain.ApplyType;
import hotspot.admin.family.domain.FamilyApplyStatus;
import hotspot.admin.family.domain.FamilyRole;
import hotspot.admin.family.infrastructure.query.dto.FamilyRequestListRow;
import hotspot.admin.family.service.port.FamilyApplyQueryRepository;

@ExtendWith(MockitoExtension.class)
class GetFamilyRequestListServiceTest {

    @Mock
    private FamilyApplyQueryRepository familyApplyQueryRepository;

    @Mock
    private PhoneCryptoUtil phoneCryptoUtil;

    private GetFamilyRequestListServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GetFamilyRequestListServiceImpl(familyApplyQueryRepository, phoneCryptoUtil);
    }

    @Test
    @DisplayName("가족 요청 목록 조회 시 요청자/대상자 전화번호를 마스킹한다")
    void getFamilyRequestsSuccess() throws Exception {
        FamilyRequestListRow row = FamilyRequestListRow.builder()
                .requestId(5L)
                .familyId(19L)
                .requestSubId(100L)
                .requesterName("요청자")
                .requesterPhoneNumberEnc("enc-requester")
                .targetSubId(101L)
                .targetName("대상자")
                .targetPhoneNumberEnc("enc-target")
                .targetFamilyRole(FamilyRole.PARENT)
                .relationDocumentUrl("https://doc.example")
                .requestedAt(LocalDateTime.of(2026, 2, 24, 10, 30))
                .build();

        FamilyRequestListRequest request = new FamilyRequestListRequest();
        request.setPage(0);
        request.setSize(20);

        when(familyApplyQueryRepository.countFamilyRequestList(ApplyType.ADD, FamilyApplyStatus.PENDING))
                .thenReturn(1L);
        when(familyApplyQueryRepository.findFamilyRequestList(ApplyType.ADD, FamilyApplyStatus.PENDING, 20, 0))
                .thenReturn(List.of(row));
        when(phoneCryptoUtil.decryptPhone("enc-requester"))
                .thenReturn("01011112222");
        when(phoneCryptoUtil.decryptPhone("enc-target"))
                .thenReturn("01033334444");

        FamilyRequestListResponse result = service.getFamilyRequests(ApplyType.ADD, FamilyApplyStatus.PENDING, request);

        assertThat(result.page()).isEqualTo(0);
        assertThat(result.size()).isEqualTo(20);
        assertThat(result.totalElements()).isEqualTo(1L);
        assertThat(result.totalPages()).isEqualTo(1);
        assertThat(result.hasNext()).isFalse();
        assertThat(result.requests()).hasSize(1);
        FamilyRequestListItem item = result.requests().get(0);
        assertThat(item.requestDisplayId()).isEqualTo("REQ-005");
        assertThat(item.familyDisplayId()).isEqualTo("FAM-000019");
        assertThat(item.familyName()).isEqualTo("요청자 가족");
        assertThat(item.requesterPhoneNumber()).isEqualTo("010-****-2222");
        assertThat(item.targets()).hasSize(1);
        FamilyRequestTargetItem target = item.targets().get(0);
        assertThat(target.targetPhoneNumber()).isEqualTo("010-****-4444");
        assertThat(target.targetFamilyRole()).isEqualTo(FamilyRole.PARENT);
    }

    @Test
    @DisplayName("전화번호 복호화 실패 시 예외를 던진다")
    void decryptFailThenException() throws Exception {
        FamilyRequestListRow row = FamilyRequestListRow.builder()
                .requestId(1L)
                .familyId(1L)
                .requestSubId(10L)
                .requesterName("요청자")
                .requesterPhoneNumberEnc("enc-requester")
                .targetSubId(11L)
                .targetName("대상자")
                .targetPhoneNumberEnc("enc-target")
                .targetFamilyRole(FamilyRole.CHILD)
                .relationDocumentUrl("https://doc.example")
                .requestedAt(LocalDateTime.of(2026, 2, 24, 10, 30))
                .build();

        FamilyRequestListRequest request = new FamilyRequestListRequest();
        request.setPage(0);
        request.setSize(20);

        when(familyApplyQueryRepository.countFamilyRequestList(ApplyType.REMOVE, FamilyApplyStatus.APPROVED))
                .thenReturn(1L);
        when(familyApplyQueryRepository.findFamilyRequestList(ApplyType.REMOVE, FamilyApplyStatus.APPROVED, 20, 0))
                .thenReturn(List.of(row));
        when(phoneCryptoUtil.decryptPhone("enc-requester"))
                .thenThrow(new GeneralSecurityException("decrypt failed"));

        assertThatThrownBy(() -> service.getFamilyRequests(ApplyType.REMOVE, FamilyApplyStatus.APPROVED, request))
                .isInstanceOf(ApplicationException.class)
                .matches(ex -> ((ApplicationException) ex).getCode() == FamilyErrorCode.PHONE_DECRYPT_FAILED);
    }
}
