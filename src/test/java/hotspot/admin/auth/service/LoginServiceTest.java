package hotspot.admin.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.admin.auth.controller.request.LoginRequest;
import hotspot.admin.auth.controller.response.TokenResponse;
import hotspot.admin.auth.service.port.TokenProvider;

@ExtendWith(MockitoExtension.class)
public class LoginServiceTest {

    @Mock
    private TokenProvider tokenProvider;

    private LoginServiceImpl loginService;

    @BeforeEach
    void setUp() {
        loginService = new LoginServiceImpl(tokenProvider, "admin123");
    }

    @Test
    void loginSuccess() {

        // given
        String adminCode = "admin123";
        LoginRequest request = new LoginRequest(adminCode);

        when(tokenProvider.createToken())
                .thenReturn("mock-token");

        // when
        TokenResponse response = loginService.login(request);

        // then
        assertThat(response.getAccessToken()).isEqualTo("mock-token");
    }
}
