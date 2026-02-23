package hotspot.admin.policy.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hotspot.admin.common.ApiResponse;
import hotspot.admin.policy.controller.port.DeletePolicyService;
import hotspot.admin.policy.controller.port.GetPolicyListService;
import hotspot.admin.policy.controller.port.UpdatePolicyActiveService;
import hotspot.admin.policy.controller.request.PolicyListRequest;
import hotspot.admin.policy.controller.request.UpdatePolicyActiveRequest;
import hotspot.admin.policy.controller.response.AppPolicyListResponse;
import hotspot.admin.policy.controller.response.TimePolicyListResponse;
import hotspot.admin.policy.controller.response.UpdatePolicyActiveResponse;
import hotspot.admin.policy.domain.AdminPolicyType;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/policies")
public class PolicyController {

    private final GetPolicyListService getPolicyListService;
    private final UpdatePolicyActiveService updatePolicyActiveService;
    private final DeletePolicyService deletePolicyService;

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

    @DeleteMapping("/{policyType}/{policyId}")
    public ResponseEntity<ApiResponse<Void>> deletePolicy(
            @PathVariable String policyType,
            @PathVariable Long policyId
    ) {
        deletePolicyService.deletePolicy(AdminPolicyType.from(policyType), policyId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PatchMapping("/{policyType}/{policyId}/active")
    public ResponseEntity<ApiResponse<UpdatePolicyActiveResponse>> updatePolicyActive(
            @PathVariable String policyType,
            @PathVariable Long policyId,
            @Valid @RequestBody UpdatePolicyActiveRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                updatePolicyActiveService.updatePolicyActive(
                        AdminPolicyType.from(policyType),
                        policyId,
                        request.isActive()
                )
        ));
    }
}
