package hotspot.admin.auth.controller.port;

import hotspot.admin.auth.controller.request.LoginRequest;
import hotspot.admin.auth.controller.response.TokenResponse;

public interface LoginService {
    TokenResponse login(LoginRequest request);
}
