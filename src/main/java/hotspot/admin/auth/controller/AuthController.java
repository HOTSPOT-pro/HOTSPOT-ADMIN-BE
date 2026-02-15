package hotspot.admin.auth.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hotspot.admin.auth.controller.port.LoginService;
import hotspot.admin.auth.controller.request.LoginRequest;
import hotspot.admin.auth.controller.response.TokenResponse;
import hotspot.admin.common.ApiResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginService loginService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(loginService.login(request)));
    }
}
