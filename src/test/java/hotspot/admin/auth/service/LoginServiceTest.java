package hotspot.admin.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.admin.auth.controller.request.LoginRequest;
import hotspot.admin.auth.controller.response.TokenResponse;
import hotspot.admin.auth.domain.Admin;
import hotspot.admin.auth.service.port.AdminRepository;
import hotspot.admin.common.jwt.JwtProvider;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LoginServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private LoginServiceImpl loginService;

    @Test
    void loginSuccess() {

        // given
        String adminCode = "admin123";
        LoginRequest request = new LoginRequest(adminCode);

        Admin admin = Admin.builder()
                .adminId(1L)
                .adminCode(adminCode)
                .build();

        when(adminRepository.findByAdminCode(adminCode))
                .thenReturn(admin);

        when(jwtProvider.createToken(adminCode))
                .thenReturn("mock-token");

        // when
        TokenResponse response = loginService.login(request);

        // then
        assertThat(response.getAccessToken()).isEqualTo("mock-token");
    }
}
