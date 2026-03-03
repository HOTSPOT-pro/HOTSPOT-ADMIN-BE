package hotspot.admin.family.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import hotspot.admin.family.controller.port.GetFamilyRequestListService;
import hotspot.admin.family.controller.port.ProcessFamilyRequestService;
import hotspot.admin.family.controller.response.FamilyRequestListItem;
import hotspot.admin.family.controller.response.FamilyRequestListResponse;
import hotspot.admin.family.controller.response.FamilyRequestTargetItem;
import hotspot.admin.family.domain.ApplyType;
import hotspot.admin.family.domain.FamilyApplyStatus;
import hotspot.admin.family.domain.FamilyRole;

@WebMvcTest(FamilyApplyController.class)
@AutoConfigureMockMvc(addFilters = false)
class FamilyApplyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetFamilyRequestListService getFamilyRequestListService;

    @MockBean
    private ProcessFamilyRequestService processFamilyRequestService;

    @Test
    @DisplayName("가족 요청 목록 조회 성공")
    void getFamilyRequestsSuccess() throws Exception {
        FamilyRequestListItem item = FamilyRequestListItem.builder()
                .requestId(13L)
                .requestDisplayId("REQ-013")
                .familyId(2L)
                .familyDisplayId("FAM-000002")
                .familyName("가족대표 가족")
                .requesterName("가족대표")
                .requesterPhoneNumber("010-****-1111")
                .targets(List.of(
                        FamilyRequestTargetItem.builder()
                                .targetSubId(22L)
                                .targetName("추가대상")
                                .targetPhoneNumber("010-****-2222")
                                .targetFamilyRole(FamilyRole.CHILD)
                                .build()
                ))
                .relationDocumentUrl("https://doc.example/1")
                .requestedAt(LocalDateTime.of(2026, 2, 24, 9, 30))
                .build();

        FamilyRequestListResponse response = FamilyRequestListResponse.builder()
                .page(0)
                .size(20)
                .totalElements(1L)
                .totalPages(1)
                .hasNext(false)
                .applyType(ApplyType.ADD)
                .status(FamilyApplyStatus.PENDING)
                .requests(List.of(item))
                .build();

        when(getFamilyRequestListService.getFamilyRequests(eq(ApplyType.ADD), eq(FamilyApplyStatus.PENDING), any()))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/families/requests/add/pending")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.applyType").value("ADD"))
                .andExpect(jsonPath("$.data.requests[0].requestDisplayId").value("REQ-013"))
                .andExpect(jsonPath("$.data.requests[0].targets[0].targetName").value("추가대상"));
    }

    @Test
    @DisplayName("가족 요청 승인 성공")
    void approveFamilyRequestSuccess() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/families/requests/remove/99/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("가족 요청 거절 성공")
    void rejectFamilyRequestSuccess() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/families/requests/add/100/reject"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());
    }
}
