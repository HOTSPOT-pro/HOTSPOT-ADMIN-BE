package hotspot.admin.policy.service;

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
import hotspot.admin.policy.domain.AdminPolicyType;
import hotspot.admin.policy.service.port.AppBlockedServiceRepository;
import hotspot.admin.policy.service.port.BlockPolicyRepository;

@ExtendWith(MockitoExtension.class)
class DeletePolicyServiceImplTest {

    @Mock
    private BlockPolicyRepository blockPolicyRepository;

    @Mock
    private AppBlockedServiceRepository appBlockedServiceRepository;

    private DeletePolicyServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DeletePolicyServiceImpl(blockPolicyRepository, appBlockedServiceRepository);
    }

    @Test
    @DisplayName("시간 정책 삭제 성공")
    void deleteTimePolicySuccess() {
        when(blockPolicyRepository.softDeleteById(1L)).thenReturn(1);
        service.deletePolicy(AdminPolicyType.TIME, 1L);
    }

    @Test
    @DisplayName("앱 정책 삭제 성공")
    void deleteAppPolicySuccess() {
        when(appBlockedServiceRepository.softDeleteById(2L)).thenReturn(1);
        service.deletePolicy(AdminPolicyType.APP, 2L);
    }

    @Test
    @DisplayName("삭제 대상이 없으면 예외")
    void deletePolicyNotFound() {
        when(blockPolicyRepository.softDeleteById(99L)).thenReturn(0);

        assertThatThrownBy(() -> service.deletePolicy(AdminPolicyType.TIME, 99L))
                .isInstanceOf(ApplicationException.class)
                .matches(ex -> ((ApplicationException) ex).getCode() == PolicyErrorCode.POLICY_NOT_FOUND);
    }
}
