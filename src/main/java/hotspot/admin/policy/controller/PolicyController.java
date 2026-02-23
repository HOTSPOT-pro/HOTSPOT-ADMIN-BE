package hotspot.admin.policy.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hotspot.admin.common.ApiResponse;
import hotspot.admin.policy.controller.port.GetPolicyListService;
import hotspot.admin.policy.controller.request.PolicyListRequest;
import hotspot.admin.policy.controller.response.AppPolicyListResponse;
import hotspot.admin.policy.controller.response.TimePolicyListResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/policies")
public class PolicyController {

    private final GetPolicyListService getPolicyListService;

    @GetMapping("/time")
    public ResponseEntity<ApiResponse<TimePolicyListResponse>> getTimePolicies(
            @Valid @ModelAttribute PolicyListRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(getPolicyListService.getTimePolicies(request)));
    }

    @GetMapping("/app")
    public ResponseEntity<ApiResponse<AppPolicyListResponse>> getAppPolicies(
            @Valid @ModelAttribute PolicyListRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(getPolicyListService.getAppPolicies(request)));
    }
}
