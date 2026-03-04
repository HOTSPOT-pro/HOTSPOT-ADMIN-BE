package hotspot.admin.family.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hotspot.admin.common.ApiResponse;
import hotspot.admin.family.controller.port.GetFamilyControlStatusService;
import hotspot.admin.family.controller.port.GetFamilyListService;
import hotspot.admin.family.controller.port.GetFamilyPolicyDetailStatusService;
import hotspot.admin.family.controller.port.GetFamilyPolicyStatusService;
import hotspot.admin.family.controller.port.GetFamilySummaryService;
import hotspot.admin.family.controller.port.SearchFamilyByPhoneService;
import hotspot.admin.family.controller.port.UpdateFamilyMemberControlStatusService;
import hotspot.admin.family.controller.port.UpdateFamilyPriorityTypeService;
import hotspot.admin.family.controller.request.FamilyListRequest;
import hotspot.admin.family.controller.request.UpdateFamilyMemberControlStatusRequest;
import hotspot.admin.family.controller.request.UpdateFamilyPriorityRequest;
import hotspot.admin.family.controller.response.FamilyControlStatusResponse;
import hotspot.admin.family.controller.response.FamilyListResponse;
import hotspot.admin.family.controller.response.FamilyPhoneSearchResponse;
import hotspot.admin.family.controller.response.FamilyPolicyMemberDetailItem;
import hotspot.admin.family.controller.response.FamilyPolicyMemberStatusItem;
import hotspot.admin.family.controller.response.FamilySummaryResponse;
import hotspot.admin.family.controller.swagger.FamilyApi;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/families")
public class FamilyController implements FamilyApi {
    private final GetFamilyListService getFamilyListService;
    private final GetFamilySummaryService getFamilySummaryService;
    private final GetFamilyControlStatusService getFamilyControlStatusService;
    private final GetFamilyPolicyStatusService getFamilyPolicyStatusService;
    private final GetFamilyPolicyDetailStatusService getFamilyPolicyDetailStatusService;
    private final SearchFamilyByPhoneService searchFamilyByPhoneService;
    private final UpdateFamilyMemberControlStatusService updateFamilyMemberControlStatusService;
    private final UpdateFamilyPriorityTypeService updateFamilyPriorityTypeService;

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

    /** 가족 제어 기능 탭에서 특정 구성원의 데이터 한도/잠금 상태를 수정한다. */
    @Override
    @PatchMapping("/{familyId}/member/{subId}/control-status")
    public ResponseEntity<ApiResponse<Void>> updateFamilyMemberControlStatus(
            @PathVariable Long familyId,
            @PathVariable Long subId,
            @Valid @RequestBody UpdateFamilyMemberControlStatusRequest request
    ) {
        updateFamilyMemberControlStatusService.updateMemberControlStatus(
                familyId,
                subId,
                request.dataLimitGb(),
                request.isBlocked()
        );
        return ResponseEntity.ok(ApiResponse.success());
    }

    /** 가족 상세 정책 적용 탭(구성원별 시간/서비스 정책 적용 현황)을 조회한다. */
    @Override
    @GetMapping("/{familyId}/policy-status")
    public ResponseEntity<ApiResponse<List<FamilyPolicyMemberStatusItem>>> getFamilyPolicyStatus(
            @PathVariable Long familyId) {
        return ResponseEntity.ok(ApiResponse.success(getFamilyPolicyStatusService.getFamilyPolicyStatus(familyId)));
    }

    /** 가족 상세 정책 적용 탭에서 특정 구성원의 정책별 적용 여부를 조회한다. */
    @Override
    @GetMapping("/{familyId}/members/{subId}/policy-status")
    public ResponseEntity<ApiResponse<FamilyPolicyMemberDetailItem>> getFamilyPolicyDetailStatus(
            @PathVariable Long familyId,
            @PathVariable Long subId) {
        return ResponseEntity.ok(ApiResponse.success(
                getFamilyPolicyDetailStatusService.getFamilyPolicyDetailStatus(familyId, subId)
        ));
    }

    /** 가족 제어 기능의 우선순위 유형(FIFO/PRIORITY)을 변경한다. */
    @Override
    @PatchMapping("/{familyId}/priority")
    public ResponseEntity<ApiResponse<Void>> updateFamilyPriority(
            @PathVariable Long familyId,
            @Valid @RequestBody UpdateFamilyPriorityRequest request
    ) {
        updateFamilyPriorityTypeService.updatePriorityType(
                familyId,
                request.priorityType(),
                request.memberPriorities()
        );
        return ResponseEntity.ok(ApiResponse.success());
    }

    /** 전화번호로 가족을 검색한다. */
    @Override
    @GetMapping("/search/phone")
    public ResponseEntity<ApiResponse<FamilyPhoneSearchResponse>> searchFamilyByPhone(
            @RequestParam String phoneNumber) {
        return ResponseEntity.ok(ApiResponse.success(searchFamilyByPhoneService.searchByPhone(phoneNumber)));
    }
}
