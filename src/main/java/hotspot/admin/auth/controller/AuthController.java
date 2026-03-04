package hotspot.admin.auth.controller;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hotspot.admin.auth.controller.port.LoginService;
import hotspot.admin.auth.controller.request.LoginRequest;
import hotspot.admin.auth.controller.response.TokenResponse;
import hotspot.admin.auth.controller.swagger.AuthApi;
import hotspot.admin.common.ApiResponse;
import hotspot.admin.common.util.cookie.CookieUtil;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final LoginService loginService;
    @Value("${jwt.expiration}")
    private long accessTokenExpiration;

    @Override
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Void>> login(@RequestBody @Valid LoginRequest request) {
        TokenResponse tokenResponse = loginService.login(request);

        ResponseCookie accessTokenCookie = CookieUtil.createCookie(
                "accessToken",
                tokenResponse.getAccessToken(),
                accessTokenExpiration
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .body(ApiResponse.success());
    }
}
