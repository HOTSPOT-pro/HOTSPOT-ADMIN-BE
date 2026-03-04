package hotspot.admin.family.controller.swagger;

import java.util.List;

import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import hotspot.admin.common.exception.ErrorResponse;
import hotspot.admin.family.controller.request.FamilyListRequest;
import hotspot.admin.family.controller.request.UpdateFamilyMemberControlStatusRequest;
import hotspot.admin.family.controller.request.UpdateFamilyMemberPoliciesRequest;
import hotspot.admin.family.controller.request.UpdateFamilyPriorityRequest;
import hotspot.admin.family.controller.response.FamilyControlStatusResponse;
import hotspot.admin.family.controller.response.FamilyListResponse;
import hotspot.admin.family.controller.response.FamilyMemberAppPolicyStatusResponse;
import hotspot.admin.family.controller.response.FamilyMemberTimePolicyStatusResponse;
import hotspot.admin.family.controller.response.FamilyPhoneSearchResponse;
import hotspot.admin.family.controller.response.FamilyPolicyMemberStatusItem;
import hotspot.admin.family.controller.response.FamilySummaryResponse;
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

    @Operation(summary = "가족 구성원 제어 상태 수정", description = "특정 구성원의 데이터 한도(GB), 차단 여부, 부모 권한 여부를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = """
                    잘못된 요청
                    - COMMON_002: 올바르지 않은 요청입니다.
                    - FAMILY_016: 데이터 한도는 0 이상이어야 합니다.
                    - FAMILY_017: 데이터 한도는 가족 공유 데이터량을 초과할 수 없습니다.
                    - FAMILY_018: 가족 대표(OWNER)의 부모 권한은 변경할 수 없습니다.
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
                    조회 대상이 없음
                    - FAMILY_001: 가족 정보를 찾을 수 없습니다.
                    - FAMILY_010: 가족 구성원 정보를 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = """
                    서버 에러
                    - COMMON_004: 서버 에러가 발생했습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{familyId}/members/{subId}/control-status")
    ResponseEntity<hotspot.admin.common.ApiResponse<Void>> updateFamilyMemberControlStatus(
            @Parameter(description = "가족 ID", example = "1") @PathVariable Long familyId,
            @Parameter(description = "구성원 구독 ID", example = "101") @PathVariable Long subId,
            @Valid @RequestBody UpdateFamilyMemberControlStatusRequest request);

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

    @Operation(summary = "구성원 시간 정책 조회", description = "특정 구성원의 시간 정책 목록과 정책별 적용 여부를 조회합니다.")
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
                    조회 대상이 없음
                    - FAMILY_001: 가족 정보를 찾을 수 없습니다.
                    - FAMILY_010: 가족 구성원 정보를 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = """
                    서버 에러
                    - COMMON_004: 서버 에러가 발생했습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{familyId}/members/{subId}/policy-status/time")
    ResponseEntity<hotspot.admin.common.ApiResponse<FamilyMemberTimePolicyStatusResponse>> getFamilyMemberTimePolicyStatus(
            @Parameter(description = "가족 ID", example = "1") @PathVariable Long familyId,
            @Parameter(description = "구성원 구독 ID", example = "101") @PathVariable Long subId);

    @Operation(summary = "구성원 앱 정책 조회", description = "특정 구성원의 앱 정책 목록과 정책별 적용 여부를 조회합니다.")
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
                    조회 대상이 없음
                    - FAMILY_001: 가족 정보를 찾을 수 없습니다.
                    - FAMILY_010: 가족 구성원 정보를 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = """
                    서버 에러
                    - COMMON_004: 서버 에러가 발생했습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{familyId}/members/{subId}/policy-status/app")
    ResponseEntity<hotspot.admin.common.ApiResponse<FamilyMemberAppPolicyStatusResponse>> getFamilyMemberAppPolicyStatus(
            @Parameter(description = "가족 ID", example = "1") @PathVariable Long familyId,
            @Parameter(description = "구성원 구독 ID", example = "101") @PathVariable Long subId);

    @Operation(summary = "구성원 시간 정책 적용 상태 수정", description = "특정 구성원의 시간 정책별 적용 여부를 일괄 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
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
            @ApiResponse(responseCode = "404", description = """
                    조회 대상이 없음
                    - FAMILY_001: 가족 정보를 찾을 수 없습니다.
                    - FAMILY_010: 가족 구성원 정보를 찾을 수 없습니다.
                    - POLICY_001: 정책 정보를 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = """
                    서버 에러
                    - COMMON_004: 서버 에러가 발생했습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{familyId}/members/{subId}/policy-status/time")
    ResponseEntity<hotspot.admin.common.ApiResponse<Void>> updateFamilyMemberTimePolicyStatus(
            @Parameter(description = "가족 ID", example = "1") @PathVariable Long familyId,
            @Parameter(description = "구성원 구독 ID", example = "101") @PathVariable Long subId,
            @Valid @RequestBody UpdateFamilyMemberPoliciesRequest request);

    @Operation(summary = "구성원 앱 정책 적용 상태 수정", description = "특정 구성원의 앱 정책별 적용 여부를 일괄 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
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
            @ApiResponse(responseCode = "404", description = """
                    조회 대상이 없음
                    - FAMILY_001: 가족 정보를 찾을 수 없습니다.
                    - FAMILY_010: 가족 구성원 정보를 찾을 수 없습니다.
                    - POLICY_001: 정책 정보를 찾을 수 없습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = """
                    서버 에러
                    - COMMON_004: 서버 에러가 발생했습니다.
                    """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{familyId}/members/{subId}/policy-status/app")
    ResponseEntity<hotspot.admin.common.ApiResponse<Void>> updateFamilyMemberAppPolicyStatus(
            @Parameter(description = "가족 ID", example = "1") @PathVariable Long familyId,
            @Parameter(description = "구성원 구독 ID", example = "101") @PathVariable Long subId,
            @Valid @RequestBody UpdateFamilyMemberPoliciesRequest request);

    @Operation(summary = "가족 우선순위 유형 변경", description = "가족 우선순위 유형(FIFO/PRIORITY)을 변경하고 필요 시 구성원 우선순위를 반영합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "변경 성공"),
            @ApiResponse(responseCode = "400", description = """
                    잘못된 요청
                    - COMMON_001: 잘못된 요청입니다.
                    - FAMILY_011: 우선순위 모드에서는 -1 값을 사용할 수 없습니다.
                    - FAMILY_012: 중복된 우선순위 값이 존재합니다.
                    - FAMILY_013: 우선순위는 1부터 시작하여 연속적이어야 합니다.
                    - FAMILY_014: 모든 가족 구성원의 우선순위 값이 필요합니다.
                    - FAMILY_015: 중복된 구성원 우선순위 입력이 존재합니다.
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
    @PatchMapping("/{familyId}/priority")
    ResponseEntity<hotspot.admin.common.ApiResponse<Void>> updateFamilyPriority(
            @Parameter(description = "가족 ID", example = "1") @PathVariable Long familyId,
            @Valid @RequestBody UpdateFamilyPriorityRequest request);

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

}
