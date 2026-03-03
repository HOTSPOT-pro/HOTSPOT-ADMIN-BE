package hotspot.admin.family.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hotspot.admin.common.ApiResponse;
import hotspot.admin.family.controller.port.GetFamilyRequestListService;
import hotspot.admin.family.controller.port.ProcessFamilyRequestService;
import hotspot.admin.family.controller.request.FamilyRequestListRequest;
import hotspot.admin.family.controller.response.FamilyRequestListResponse;
import hotspot.admin.family.controller.swagger.FamilyApplyApi;
import hotspot.admin.family.domain.ApplyType;
import hotspot.admin.family.domain.FamilyApplyStatus;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/families")
public class FamilyApplyController implements FamilyApplyApi {

    private final GetFamilyRequestListService getFamilyRequestListService;
    private final ProcessFamilyRequestService processFamilyRequestService;

    /** 가족 요청(생성/추가/삭제) 목록을 상태별로 조회한다. */
    @Override
    @GetMapping("/requests/{applyType}/{status}")
    public ResponseEntity<ApiResponse<FamilyRequestListResponse>> getFamilyRequests(
            @PathVariable ApplyType applyType,
            @PathVariable FamilyApplyStatus status,
            @Valid @ModelAttribute FamilyRequestListRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                getFamilyRequestListService.getFamilyRequests(applyType, status, request)
        ));
    }

    /** 가족 요청을 승인 처리한다. */
    @Override
    @PatchMapping("/requests/{applyType}/{requestId}/approve")
    public ResponseEntity<ApiResponse<Void>> approveFamilyRequest(
            @PathVariable ApplyType applyType,
            @PathVariable Long requestId
    ) {
        processFamilyRequestService.approve(applyType, requestId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /** 가족 요청을 반려 처리한다. */
    @Override
    @PatchMapping("/requests/{applyType}/{requestId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectFamilyRequest(
            @PathVariable ApplyType applyType,
            @PathVariable Long requestId
    ) {
        processFamilyRequestService.reject(applyType, requestId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
