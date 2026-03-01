package hotspot.admin.policy.controller.swagger;

import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import hotspot.admin.common.exception.ErrorResponse;
import hotspot.admin.policy.controller.request.CreateAppPolicyRequest;
import hotspot.admin.policy.controller.request.CreateTimePolicyRequest;
import hotspot.admin.policy.controller.request.PolicyListRequest;
import hotspot.admin.policy.controller.request.UpdatePolicyActiveRequest;
import hotspot.admin.policy.controller.response.AppPolicyListResponse;
import hotspot.admin.policy.controller.response.CreateAppPolicyResponse;
import hotspot.admin.policy.controller.response.CreateTimePolicyResponse;
import hotspot.admin.policy.controller.response.TimePolicyListResponse;
import hotspot.admin.policy.controller.response.UpdatePolicyActiveResponse;
import hotspot.admin.policy.domain.AdminPolicyType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Admin Policy", description = "관리자 정책 관리 API")
public interface PolicyApi {

    @Operation(summary = "시간 정책 목록 조회", description = "관리자 시간 정책 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = """
                    잘못된 요청
                    - COMMON_002: 올바르지 않은 요청입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = """
                    인증 실패
                    - AUTH_002: 유효하지 않은 토큰입니다.
                    - AUTH_003: 만료된 토큰입니다.
                    - AUTH_004: 지원되지 않는 토큰입니다.
                    - AUTH_005: 토큰이 비어있거나 잘못되었습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = """
                    서버 에러
                    - COMMON_004: 서버 에러가 발생했습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<hotspot.admin.common.ApiResponse<TimePolicyListResponse>> getTimePolicies(
            @Valid @ParameterObject @ModelAttribute PolicyListRequest request);

    @Operation(summary = "앱 정책 목록 조회", description = "관리자 앱 정책 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = """
                    잘못된 요청
                    - COMMON_002: 올바르지 않은 요청입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = """
                    인증 실패
                    - AUTH_002: 유효하지 않은 토큰입니다.
                    - AUTH_003: 만료된 토큰입니다.
                    - AUTH_004: 지원되지 않는 토큰입니다.
                    - AUTH_005: 토큰이 비어있거나 잘못되었습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = """
                    서버 에러
                    - COMMON_004: 서버 에러가 발생했습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<hotspot.admin.common.ApiResponse<AppPolicyListResponse>> getAppPolicies(
            @Valid @ParameterObject @ModelAttribute PolicyListRequest request);

    @Operation(summary = "시간 정책 생성", description = "새로운 시간 정책을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = """
                    잘못된 요청
                    - COMMON_002: 올바르지 않은 요청입니다.
                    - POLICY_003: 정책 스냅샷 형식이 올바르지 않습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = """
                    인증 실패
                    - AUTH_002: 유효하지 않은 토큰입니다.
                    - AUTH_003: 만료된 토큰입니다.
                    - AUTH_004: 지원되지 않는 토큰입니다.
                    - AUTH_005: 토큰이 비어있거나 잘못되었습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = """
                    중복 정책명/정책코드
                    - POLICY_004: 동일한 정책명과 정책유형이 이미 존재합니다.
                    - POLICY_005: 동일한 정책 코드가 이미 존재합니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = """
                    서버 에러
                    - COMMON_004: 서버 에러가 발생했습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<hotspot.admin.common.ApiResponse<CreateTimePolicyResponse>> createTimePolicy(
            @Valid @RequestBody CreateTimePolicyRequest request);

    @Operation(summary = "앱 정책 생성", description = "새로운 앱 정책을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = """
                    잘못된 요청
                    - COMMON_002: 올바르지 않은 요청입니다.
                    - POLICY_003: 정책 스냅샷 형식이 올바르지 않습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = """
                    인증 실패
                    - AUTH_002: 유효하지 않은 토큰입니다.
                    - AUTH_003: 만료된 토큰입니다.
                    - AUTH_004: 지원되지 않는 토큰입니다.
                    - AUTH_005: 토큰이 비어있거나 잘못되었습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = """
                    중복 정책명/정책코드
                    - POLICY_004: 동일한 정책명과 정책유형이 이미 존재합니다.
                    - POLICY_005: 동일한 정책 코드가 이미 존재합니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = """
                    서버 에러
                    - COMMON_004: 서버 에러가 발생했습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<hotspot.admin.common.ApiResponse<CreateAppPolicyResponse>> createAppPolicy(
            @Valid @RequestBody CreateAppPolicyRequest request);

    @Operation(summary = "정책 삭제", description = "정책 타입과 ID로 정책을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "400", description = """
                    잘못된 요청
                    - COMMON_001: 잘못된 요청입니다.
                    - POLICY_002: 유효하지 않은 정책 타입입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = """
                    인증 실패
                    - AUTH_002: 유효하지 않은 토큰입니다.
                    - AUTH_003: 만료된 토큰입니다.
                    - AUTH_004: 지원되지 않는 토큰입니다.
                    - AUTH_005: 토큰이 비어있거나 잘못되었습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = """
                    정책 정보를 찾을 수 없음
                    - POLICY_001: 정책 정보를 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = """
                    서버 에러
                    - COMMON_004: 서버 에러가 발생했습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<hotspot.admin.common.ApiResponse<Void>> deletePolicy(
            @Parameter(
                    description = "정책 타입",
                    schema = @Schema(allowableValues = {"TIME", "APP"}, example = "TIME")
            ) @PathVariable AdminPolicyType policyType,
            @Parameter(description = "정책 ID", example = "1") @PathVariable Long policyId);

    @Operation(summary = "정책 활성화 수정", description = "정책의 활성화 여부를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = """
                    잘못된 요청
                    - COMMON_001: 잘못된 요청입니다.
                    - COMMON_002: 올바르지 않은 요청입니다.
                    - POLICY_002: 유효하지 않은 정책 타입입니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = """
                    인증 실패
                    - AUTH_002: 유효하지 않은 토큰입니다.
                    - AUTH_003: 만료된 토큰입니다.
                    - AUTH_004: 지원되지 않는 토큰입니다.
                    - AUTH_005: 토큰이 비어있거나 잘못되었습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = """
                    정책 정보를 찾을 수 없음
                    - POLICY_001: 정책 정보를 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = """
                    서버 에러
                    - COMMON_004: 서버 에러가 발생했습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<hotspot.admin.common.ApiResponse<UpdatePolicyActiveResponse>> updatePolicyActive(
            @Parameter(
                    description = "정책 타입",
                    schema = @Schema(allowableValues = {"TIME", "APP"}, example = "TIME")
            ) @PathVariable AdminPolicyType policyType,
            @Parameter(description = "정책 ID", example = "1") @PathVariable Long policyId,
            @Valid @RequestBody UpdatePolicyActiveRequest request);
}
