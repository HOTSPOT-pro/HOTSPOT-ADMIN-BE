package hotspot.admin.auth.controller.swagger;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import hotspot.admin.auth.controller.request.LoginRequest;
import hotspot.admin.auth.controller.response.TokenResponse;
import hotspot.admin.common.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Admin Auth", description = "관리자 인증 API")
public interface AuthApi {

    @Operation(
            summary = "관리자 로그인",
            description = "관리자 계정으로 로그인하여 JWT 토큰을 발급받습니다.",
            security = @SecurityRequirement(name = "")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                            잘못된 요청
                            - COMMON_002: 올바르지 않은 요청입니다.
                            """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = """
                            인증 실패
                            - AUTH_001: 로그인에 실패했습니다. 코드를 확인해주세요.
                            """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = """
                            서버 에러
                            - COMMON_004: 서버 에러가 발생했습니다.
                            """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    ResponseEntity<hotspot.admin.common.ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request);
}
