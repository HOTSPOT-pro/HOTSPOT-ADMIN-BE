package hotspot.admin.usage.subscriptionUsage.controller.swagger;

import hotspot.admin.usage.subscriptionUsage.controller.response.SubscriptionUsageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.ErrorResponse;

import java.util.List;

@Tag(name = "Subscription Usage", description = "회선 개인 데이터 및 선물 데이터 사용량 조회 API")
public interface SubscriptionUsageApi {

    @Operation(summary = "개인 데이터 및 선물 데이터조회",
            description = "개인 데이터 사용량, 선물 데이터 사용량을 조회할 수 있습니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "찾을 수 없음\n"
                    + "- SUBSCRIPTION_USAGE_001: 개인 데이터 전체 한도를 조회할 수 없습니다",
                    content = @Content(schema = @Schema(
                            implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류\n"
                    + "- FAMILY_003: 전화번호 복호화에 실패했습니다",
                    content = @Content(schema = @Schema(
                            implementation = ErrorResponse.class)))
    })
    ResponseEntity<hotspot.admin.common.ApiResponse<List<SubscriptionUsageResponse>>> findSubscriptionUsage(
            @Parameter (description = "가족 ID", example = "1") Long familyId);
}
