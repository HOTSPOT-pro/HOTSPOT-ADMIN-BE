package hotspot.admin.family.controller.swagger;

import java.util.List;

import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import hotspot.admin.common.exception.ErrorResponse;
import hotspot.admin.family.controller.request.FamilyListRequest;
import hotspot.admin.family.controller.request.FamilyRequestListRequest;
import hotspot.admin.family.controller.response.FamilyControlStatusResponse;
import hotspot.admin.family.controller.response.FamilyListResponse;
import hotspot.admin.family.controller.response.FamilyPhoneSearchResponse;
import hotspot.admin.family.controller.response.FamilyPolicyMemberStatusItem;
import hotspot.admin.family.controller.response.FamilyRequestListResponse;
import hotspot.admin.family.controller.response.FamilySummaryResponse;
import hotspot.admin.family.domain.ApplyType;
import hotspot.admin.family.domain.FamilyApplyStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Admin Family", description = "관리자 가족 관리 API")
public interface FamilyApi {

    @Operation(summary = "가족 목록 조회", description = "페이지 조건으로 가족 목록을 조회합니다.")
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
    ResponseEntity<hotspot.admin.common.ApiResponse<FamilyListResponse>> getFamilyList(
            @Valid @ParameterObject @ModelAttribute FamilyListRequest request);

    @Operation(summary = "가족 요약 조회", description = "가족 상세 상단 요약 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = """
                    인증 실패
                    - AUTH_002: 유효하지 않은 토큰입니다.
                    - AUTH_003: 만료된 토큰입니다.
                    - AUTH_004: 지원되지 않는 토큰입니다.
                    - AUTH_005: 토큰이 비어있거나 잘못되었습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = """
                    가족 정보를 찾을 수 없음
                    - FAMILY_001: 가족 정보를 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = """
                    서버 에러
                    - COMMON_004: 서버 에러가 발생했습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<hotspot.admin.common.ApiResponse<FamilySummaryResponse>> getFamilySummary(
            @Parameter(description = "가족 ID", example = "1") @PathVariable Long familyId);

    @Operation(summary = "가족 제어 상태 조회", description = "우선순위 유형과 구성원별 제어 상태를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = """
                    인증 실패
                    - AUTH_002: 유효하지 않은 토큰입니다.
                    - AUTH_003: 만료된 토큰입니다.
                    - AUTH_004: 지원되지 않는 토큰입니다.
                    - AUTH_005: 토큰이 비어있거나 잘못되었습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = """
                    가족 정보를 찾을 수 없음
                    - FAMILY_001: 가족 정보를 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = """
                    서버 에러
                    - COMMON_004: 서버 에러가 발생했습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<hotspot.admin.common.ApiResponse<FamilyControlStatusResponse>> getFamilyControlStatus(
            @Parameter(description = "가족 ID", example = "1") @PathVariable Long familyId);

    @Operation(summary = "가족 정책 상태 조회", description = "구성원별 시간/서비스 정책 적용 상태를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = """
                    인증 실패
                    - AUTH_002: 유효하지 않은 토큰입니다.
                    - AUTH_003: 만료된 토큰입니다.
                    - AUTH_004: 지원되지 않는 토큰입니다.
                    - AUTH_005: 토큰이 비어있거나 잘못되었습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = """
                    가족 정보를 찾을 수 없음
                    - FAMILY_001: 가족 정보를 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = """
                    서버 에러
                    - COMMON_004: 서버 에러가 발생했습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<hotspot.admin.common.ApiResponse<List<FamilyPolicyMemberStatusItem>>> getFamilyPolicyStatus(
            @Parameter(description = "가족 ID", example = "1") @PathVariable Long familyId);

    @Operation(summary = "전화번호로 가족 검색", description = "전화번호 기준으로 가족을 검색합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = """
                    잘못된 요청
                    - COMMON_001: 잘못된 요청입니다.
                    - FAMILY_004: 전화번호 형식이 올바르지 않습니다.
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
                    검색 결과 없음
                    - FAMILY_001: 가족 정보를 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = """
                    서버 에러
                    - COMMON_004: 서버 에러가 발생했습니다.
                    - FAMILY_003: 전화번호 복호화에 실패했습니다.
                    - FAMILY_005: 전화번호 해시 생성에 실패했습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<hotspot.admin.common.ApiResponse<FamilyPhoneSearchResponse>> searchFamilyByPhone(
            @Parameter(description = "검색할 전화번호", example = "01012345678") @RequestParam String phoneNumber);

    @Operation(summary = "가족 요청 목록 조회", description = "결합/해제 요청 목록을 상태별로 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = """
                    잘못된 요청
                    - COMMON_002: 올바르지 않은 요청입니다.
                    - FAMILY_006: 유효하지 않은 가족 요청 타입입니다.
                    - FAMILY_007: 유효하지 않은 가족 요청 상태입니다.
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
    ResponseEntity<hotspot.admin.common.ApiResponse<FamilyRequestListResponse>> getFamilyRequests(
            @Parameter(
                    description = "요청 타입",
                    schema = @Schema(allowableValues = {"ADD", "REMOVE"}, example = "ADD")
            ) @PathVariable ApplyType applyType,
            @Parameter(
                    description = "요청 상태",
                    schema = @Schema(allowableValues = {"PENDING", "APPROVED", "REJECTED", "CANCELLED"},
                            example = "PENDING")
            ) @PathVariable FamilyApplyStatus status,
            @Valid @ParameterObject @ModelAttribute FamilyRequestListRequest request);

    @Operation(summary = "가족 요청 승인", description = "가족 요청을 승인 처리합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "처리 성공"),
            @ApiResponse(responseCode = "400", description = """
                    잘못된 요청
                    - COMMON_001: 잘못된 요청입니다.
                    - FAMILY_006: 유효하지 않은 가족 요청 타입입니다.
                    - FAMILY_009: 대기중 요청만 처리할 수 있습니다.
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
                    요청 정보를 찾을 수 없음
                    - FAMILY_008: 가족 요청 정보를 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = """
                    서버 에러
                    - COMMON_004: 서버 에러가 발생했습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<hotspot.admin.common.ApiResponse<Void>> approveFamilyRequest(
            @Parameter(
                    description = "요청 타입",
                    schema = @Schema(allowableValues = {"ADD", "REMOVE"}, example = "ADD")
            ) @PathVariable ApplyType applyType,
            @Parameter(description = "요청 ID", example = "10") @PathVariable Long requestId);

    @Operation(summary = "가족 요청 반려", description = "가족 요청을 반려 처리합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "처리 성공"),
            @ApiResponse(responseCode = "400", description = """
                    잘못된 요청
                    - COMMON_001: 잘못된 요청입니다.
                    - FAMILY_006: 유효하지 않은 가족 요청 타입입니다.
                    - FAMILY_009: 대기중 요청만 처리할 수 있습니다.
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
                    요청 정보를 찾을 수 없음
                    - FAMILY_008: 가족 요청 정보를 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = """
                    서버 에러
                    - COMMON_004: 서버 에러가 발생했습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<hotspot.admin.common.ApiResponse<Void>> rejectFamilyRequest(
            @Parameter(
                    description = "요청 타입",
                    schema = @Schema(allowableValues = {"ADD", "REMOVE"}, example = "ADD")
            ) @PathVariable ApplyType applyType,
            @Parameter(description = "요청 ID", example = "10") @PathVariable Long requestId);
}
