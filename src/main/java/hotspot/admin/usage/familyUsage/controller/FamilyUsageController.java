package hotspot.admin.usage.familyUsage.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hotspot.admin.common.ApiResponse;
import hotspot.admin.usage.familyUsage.controller.port.FindFamilyUsageService;
import hotspot.admin.usage.familyUsage.controller.response.FamilyUsageResponse;
import hotspot.admin.usage.familyUsage.controller.swagger.FamilyUsageApi;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/familyUsage")
public class FamilyUsageController implements FamilyUsageApi {

    private final FindFamilyUsageService findFamilyUsageService;

    @GetMapping("/{familyId}")
    public ResponseEntity<ApiResponse<FamilyUsageResponse>> findFamilyUsage(
            @PathVariable Long familyId) {
        return ResponseEntity.ok(ApiResponse.success(
                findFamilyUsageService.findFamilyUsage(familyId)));
    }
}
