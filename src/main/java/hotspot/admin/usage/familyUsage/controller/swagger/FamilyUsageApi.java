package hotspot.admin.usage.familyUsage.controller.swagger;

import hotspot.admin.usage.familyUsage.controller.response.FamilyUsageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Family Usage", description = "가족 공유 데이터 사용량 조회 API")
public interface FamilyUsageApi {

    @Operation(summary = "가족 공유 데이터 조회",
            description = "가족 공유 데이터 사용량, 구성원 별 가족 공유 데이터 사용량을 조회할 수 있습니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "찾을 수 없음\n"
                    + "- FAMILY_USAGE_001: 가족 공유 데이터 전체 한도를 조회할 수 없습니다\n"
                    + "- FAMILY_USAGE_002: 가족 공유 데이터 가족 공유 데이터 구성원별 한도를 조회할 수 없습니다",
                    content = @Content(schema = @Schema(
                            implementation = ErrorResponse.class)))
    })
    ResponseEntity<hotspot.admin.common.ApiResponse<FamilyUsageResponse>> findFamilyUsage(
            @Parameter(description = "가족 ID", example = "1") @PathVariable Long subId);
}
