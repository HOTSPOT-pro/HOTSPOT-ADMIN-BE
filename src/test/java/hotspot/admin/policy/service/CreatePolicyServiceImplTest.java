package hotspot.admin.policy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.admin.appservice.domain.AppBlockedService;
import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.code.PolicyErrorCode;
import hotspot.admin.policy.controller.request.CreateAppPolicyRequest;
import hotspot.admin.policy.controller.request.CreateTimePolicyRequest;
import hotspot.admin.policy.controller.response.CreateAppPolicyResponse;
import hotspot.admin.policy.controller.response.CreateTimePolicyResponse;
import hotspot.admin.policy.domain.BlockPolicy;
import hotspot.admin.policy.domain.PolicyDay;
import hotspot.admin.policy.domain.PolicySnapshot;
import hotspot.admin.policy.domain.PolicyType;
import hotspot.admin.policy.service.port.AppBlockedServiceRepository;
import hotspot.admin.policy.service.port.BlockPolicyRepository;

@ExtendWith(MockitoExtension.class)
class CreatePolicyServiceImplTest {

    @Mock
    private BlockPolicyRepository blockPolicyRepository;

    @Mock
    private AppBlockedServiceRepository appBlockedServiceRepository;

    private CreatePolicyServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CreatePolicyServiceImpl(blockPolicyRepository, appBlockedServiceRepository);
    }

    @Test
    @DisplayName("시간 정책 생성 성공 - SCHEDULED")
    void createTimePolicySuccess() {
        CreateTimePolicyRequest request = new CreateTimePolicyRequest(
                "수면모드",
                PolicyType.SCHEDULED,
                PolicySnapshot.builder()
                        .days(List.of(PolicyDay.MON, PolicyDay.TUE))
                        .startTime("00:00")
                        .endTime("07:00")
                        .build()
        );
        when(blockPolicyRepository.existsByPolicyNameAndPolicyType("수면모드", PolicyType.SCHEDULED)).thenReturn(false);
        when(blockPolicyRepository.save(org.mockito.ArgumentMatchers.any(BlockPolicy.class)))
                .thenReturn(BlockPolicy.builder()
                        .blockPolicyId(3L)
                        .policyName("수면모드")
                        .policyType(PolicyType.SCHEDULED)
                        .policySnapshot(request.policySnapshot())
                        .isActive(true)
                        .isDeleted(false)
                        .build());

        CreateTimePolicyResponse result = service.createTimePolicy(request);
        assertThat(result.policyId()).isEqualTo(3L);
        assertThat(result.displayId()).isEqualTo("TP-003");
        assertThat(result.isActive()).isTrue();
    }

    @Test
    @DisplayName("시간 정책 중복이면 예외")
    void createTimePolicyDuplicate() {
        CreateTimePolicyRequest request = new CreateTimePolicyRequest(
                "수면모드",
                PolicyType.SCHEDULED,
                PolicySnapshot.builder()
                        .days(List.of(PolicyDay.MON))
                        .startTime("00:00")
                        .endTime("07:00")
                        .build()
        );
        when(blockPolicyRepository.existsByPolicyNameAndPolicyType("수면모드", PolicyType.SCHEDULED)).thenReturn(true);

        assertThatThrownBy(() -> service.createTimePolicy(request))
                .isInstanceOf(ApplicationException.class)
                .matches(ex -> ((ApplicationException) ex).getCode() == PolicyErrorCode.DUPLICATE_POLICY_NAME_TYPE);
    }

    @Test
    @DisplayName("앱 정책 생성 성공")
    void createAppPolicySuccess() {
        CreateAppPolicyRequest request = new CreateAppPolicyRequest("유튜브", "MEDIA_YOUTUBE");
        when(appBlockedServiceRepository.existsByBlockedServiceCode("MEDIA_YOUTUBE")).thenReturn(false);
        when(appBlockedServiceRepository.save(org.mockito.ArgumentMatchers.any(AppBlockedService.class)))
                .thenReturn(AppBlockedService.builder()
                        .appBlockedServiceId(5L)
                        .blockedServiceName("유튜브")
                        .blockedServiceCode("MEDIA_YOUTUBE")
                        .isActive(true)
                        .isDeleted(false)
                        .build());

        CreateAppPolicyResponse result = service.createAppPolicy(request);
        assertThat(result.policyId()).isEqualTo(5L);
        assertThat(result.displayId()).isEqualTo("AP-005");
        assertThat(result.isActive()).isTrue();
    }

    @Test
    @DisplayName("앱 정책 코드 중복이면 예외")
    void createAppPolicyDuplicate() {
        CreateAppPolicyRequest request = new CreateAppPolicyRequest("유튜브", "MEDIA_YOUTUBE");
        when(appBlockedServiceRepository.existsByBlockedServiceCode("MEDIA_YOUTUBE")).thenReturn(true);

        assertThatThrownBy(() -> service.createAppPolicy(request))
                .isInstanceOf(ApplicationException.class)
                .matches(ex -> ((ApplicationException) ex).getCode() == PolicyErrorCode.DUPLICATE_POLICY_CODE);
    }
}
