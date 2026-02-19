package hotspot.admin.family.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hotspot.admin.common.ApiResponse;
import hotspot.admin.family.controller.port.GetFamilyListService;
import hotspot.admin.family.controller.request.FamilyListRequest;
import hotspot.admin.family.controller.response.FamilyListResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/families")
public class FamilyController {
    private final GetFamilyListService getFamilyListService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<FamilyListResponse>> getFamilyList(
            @Valid @ModelAttribute FamilyListRequest request) {
        return ResponseEntity.ok(ApiResponse.success(getFamilyListService.getFamilyList(request)));
    }
}
