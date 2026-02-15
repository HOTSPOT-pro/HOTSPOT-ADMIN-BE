package hotspot.admin.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import hotspot.admin.auth.controller.port.LoginService;
import hotspot.admin.auth.controller.request.LoginRequest;
import hotspot.admin.auth.controller.response.TokenResponse;
import hotspot.admin.auth.service.port.TokenProvider;
import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.code.AuthErrorCode;

@Service
public class LoginServiceImpl implements LoginService {

    private final TokenProvider tokenProvider;
    private final String adminCode;

    public LoginServiceImpl(
            TokenProvider tokenProvider,
            @Value("${project.admin-code}") String adminCode) {
        this.tokenProvider = tokenProvider;
        this.adminCode = adminCode;
    }

    @Override
    public TokenResponse login(LoginRequest request) {
        if (!adminCode.equals(request.getAdminCode())) {
            throw new ApplicationException(AuthErrorCode.LOGIN_FAILED);
        }

        String accessToken = tokenProvider.createToken();

        return new TokenResponse(accessToken);
    }

}
