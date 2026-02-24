package hotspot.admin.policy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.code.PolicyErrorCode;
import hotspot.admin.policy.controller.response.UpdatePolicyActiveResponse;
import hotspot.admin.policy.domain.AdminPolicyType;
import hotspot.admin.policy.service.port.AppBlockedServiceRepository;
import hotspot.admin.policy.service.port.BlockPolicyRepository;

@ExtendWith(MockitoExtension.class)
class UpdatePolicyActiveServiceImplTest {

    @Mock
    private BlockPolicyRepository blockPolicyRepository;

    @Mock
    private AppBlockedServiceRepository appBlockedServiceRepository;

    private UpdatePolicyActiveServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UpdatePolicyActiveServiceImpl(blockPolicyRepository, appBlockedServiceRepository);
    }

    @Test
    @DisplayName("시간 정책 활성화/비활성화 성공")
    void updateTimePolicyActiveSuccess() {
        when(blockPolicyRepository.updateActiveById(1L, true)).thenReturn(1);

        UpdatePolicyActiveResponse result = service.updatePolicyActive(AdminPolicyType.TIME, 1L, true);
        assertThat(result.policyId()).isEqualTo(1L);
        assertThat(result.displayId()).isEqualTo("TP-001");
        assertThat(result.isActive()).isTrue();
    }

    @Test
    @DisplayName("앱 정책 활성화/비활성화 성공")
    void updateAppPolicyActiveSuccess() {
        when(appBlockedServiceRepository.updateActiveById(2L, false)).thenReturn(1);

        UpdatePolicyActiveResponse result = service.updatePolicyActive(AdminPolicyType.APP, 2L, false);
        assertThat(result.policyId()).isEqualTo(2L);
        assertThat(result.displayId()).isEqualTo("AP-002");
        assertThat(result.isActive()).isFalse();
    }

    @Test
    @DisplayName("대상이 없으면 예외")
    void updatePolicyNotFound() {
        when(blockPolicyRepository.updateActiveById(99L, true)).thenReturn(0);

        assertThatThrownBy(() -> service.updatePolicyActive(AdminPolicyType.TIME, 99L, true))
                .isInstanceOf(ApplicationException.class)
                .matches(ex -> ((ApplicationException) ex).getCode() == PolicyErrorCode.POLICY_NOT_FOUND);
    }
}
