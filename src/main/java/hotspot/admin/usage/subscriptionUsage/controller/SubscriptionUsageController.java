package hotspot.admin.usage.subscriptionUsage.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hotspot.admin.common.ApiResponse;
import hotspot.admin.usage.subscriptionUsage.controller.port.FindSubscriptionUsageService;
import hotspot.admin.usage.subscriptionUsage.controller.response.SubscriptionUsageResponse;
import hotspot.admin.usage.subscriptionUsage.controller.swagger.SubscriptionUsageApi;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/subscriptionUsage")
public class SubscriptionUsageController implements SubscriptionUsageApi {

    private final FindSubscriptionUsageService findSubscriptionUsageService;

    @Override
    @GetMapping("/{familyId}")
    public ResponseEntity<ApiResponse<List<SubscriptionUsageResponse>>> findSubscriptionUsage(
            @PathVariable Long familyId) {
        return ResponseEntity.ok(ApiResponse.success(
                findSubscriptionUsageService.findSubscriptionUsage(familyId)));
    }
}
