package hotspot.admin.family.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hotspot.admin.common.ApiResponse;
import hotspot.admin.family.controller.port.GetFamilyListService;
import hotspot.admin.family.controller.port.GetFamilyRequestListService;
import hotspot.admin.family.controller.port.SearchFamilyByPhoneService;
import hotspot.admin.family.controller.request.FamilyListRequest;
import hotspot.admin.family.controller.request.FamilyRequestListRequest;
import hotspot.admin.family.controller.response.FamilyListResponse;
import hotspot.admin.family.controller.response.FamilyPhoneSearchResponse;
import hotspot.admin.family.controller.response.FamilyRequestListResponse;
import hotspot.admin.family.domain.ApplyType;
import hotspot.admin.family.domain.FamilyApplyStatus;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/families")
public class FamilyController {
    private final GetFamilyListService getFamilyListService;
    private final SearchFamilyByPhoneService searchFamilyByPhoneService;
    private final GetFamilyRequestListService getFamilyRequestListService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<FamilyListResponse>> getFamilyList(
            @Valid @ModelAttribute FamilyListRequest request) {
        return ResponseEntity.ok(ApiResponse.success(getFamilyListService.getFamilyList(request)));
    }

    @GetMapping("/search/phone")
    public ResponseEntity<ApiResponse<FamilyPhoneSearchResponse>> searchFamilyByPhone(
            @RequestParam String phoneNumber) {
        return ResponseEntity.ok(ApiResponse.success(searchFamilyByPhoneService.searchByPhone(phoneNumber)));
    }

    @GetMapping("/requests/{applyType}/{status}")
    public ResponseEntity<ApiResponse<FamilyRequestListResponse>> getFamilyRequests(
            @PathVariable String applyType,
            @PathVariable String status,
            @Valid @ModelAttribute FamilyRequestListRequest request
    ) {
        ApplyType parsedApplyType = ApplyType.from(applyType);
        FamilyApplyStatus parsedStatus = FamilyApplyStatus.from(status);
        return ResponseEntity.ok(ApiResponse.success(
                getFamilyRequestListService.getFamilyRequests(parsedApplyType, parsedStatus, request)
        ));
    }
}
