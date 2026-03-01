package hotspot.admin.family.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hotspot.admin.common.ApiResponse;
import hotspot.admin.family.controller.port.GetFamilyControlStatusService;
import hotspot.admin.family.controller.port.GetFamilyListService;
import hotspot.admin.family.controller.port.GetFamilyPolicyStatusService;
import hotspot.admin.family.controller.port.GetFamilyRequestListService;
import hotspot.admin.family.controller.port.GetFamilySummaryService;
import hotspot.admin.family.controller.port.ProcessFamilyRequestService;
import hotspot.admin.family.controller.port.SearchFamilyByPhoneService;
import hotspot.admin.family.controller.request.FamilyListRequest;
import hotspot.admin.family.controller.request.FamilyRequestListRequest;
import hotspot.admin.family.controller.response.FamilyControlStatusResponse;
import hotspot.admin.family.controller.response.FamilyListResponse;
import hotspot.admin.family.controller.response.FamilyPhoneSearchResponse;
import hotspot.admin.family.controller.response.FamilyPolicyMemberStatusItem;
import hotspot.admin.family.controller.response.FamilyRequestListResponse;
import hotspot.admin.family.controller.response.FamilySummaryResponse;
import hotspot.admin.family.controller.swagger.FamilyApi;
import hotspot.admin.family.domain.ApplyType;
import hotspot.admin.family.domain.FamilyApplyStatus;

@RestController
@RequestMapping("/api/v1/admin/families")
public class FamilyController implements FamilyApi {
    private final GetFamilyListService getFamilyListService;
    private final GetFamilySummaryService getFamilySummaryService;
    private final GetFamilyControlStatusService getFamilyControlStatusService;
    private final GetFamilyPolicyStatusService getFamilyPolicyStatusService;
    private final SearchFamilyByPhoneService searchFamilyByPhoneService;
    private final GetFamilyRequestListService getFamilyRequestListService;
    private final ProcessFamilyRequestService processFamilyRequestService;

    public FamilyController(
            GetFamilyListService getFamilyListService,
            GetFamilySummaryService getFamilySummaryService,
            GetFamilyControlStatusService getFamilyControlStatusService,
            GetFamilyPolicyStatusService getFamilyPolicyStatusService,
            SearchFamilyByPhoneService searchFamilyByPhoneService,
            GetFamilyRequestListService getFamilyRequestListService,
            ProcessFamilyRequestService processFamilyRequestService
    ) {
        this.getFamilyListService = getFamilyListService;
        this.getFamilySummaryService = getFamilySummaryService;
        this.getFamilyControlStatusService = getFamilyControlStatusService;
        this.getFamilyPolicyStatusService = getFamilyPolicyStatusService;
        this.searchFamilyByPhoneService = searchFamilyByPhoneService;
        this.getFamilyRequestListService = getFamilyRequestListService;
        this.processFamilyRequestService = processFamilyRequestService;
    }

    /** 가족 목록을 페이지 조건으로 조회한다. */
    @Override
    @GetMapping()
    public ResponseEntity<ApiResponse<FamilyListResponse>> getFamilyList(
            @Valid @ModelAttribute FamilyListRequest request) {
        return ResponseEntity.ok(ApiResponse.success(getFamilyListService.getFamilyList(request)));
    }

    /** 가족 상세 상단 요약(대표자/전화번호/구성원 수)을 조회한다. */
    @Override
    @GetMapping("/{familyId}")
    public ResponseEntity<ApiResponse<FamilySummaryResponse>> getFamilySummary(
            @PathVariable Long familyId) {
        return ResponseEntity.ok(ApiResponse.success(getFamilySummaryService.getFamilySummary(familyId)));
    }

    /** 가족 상세 제어 기능 탭(우선순위 유형 + 구성원별 제어 상태)을 조회한다. */
    @Override
    @GetMapping("/{familyId}/control-status")
    public ResponseEntity<ApiResponse<FamilyControlStatusResponse>> getFamilyControlStatus(
            @PathVariable Long familyId) {
        return ResponseEntity.ok(ApiResponse.success(getFamilyControlStatusService.getFamilyControlStatus(familyId)));
    }

    /** 가족 상세 정책 적용 탭(구성원별 시간/서비스 정책 적용 현황)을 조회한다. */
    @Override
    @GetMapping("/{familyId}/policy-status")
    public ResponseEntity<ApiResponse<List<FamilyPolicyMemberStatusItem>>> getFamilyPolicyStatus(
            @PathVariable Long familyId) {
        return ResponseEntity.ok(ApiResponse.success(getFamilyPolicyStatusService.getFamilyPolicyStatus(familyId)));
    }

    /** 전화번호로 가족을 검색한다. */
    @Override
    @GetMapping("/search/phone")
    public ResponseEntity<ApiResponse<FamilyPhoneSearchResponse>> searchFamilyByPhone(
            @RequestParam String phoneNumber) {
        return ResponseEntity.ok(ApiResponse.success(searchFamilyByPhoneService.searchByPhone(phoneNumber)));
    }

    /** 가족 요청(결합/해제) 목록을 상태별로 조회한다. */
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
