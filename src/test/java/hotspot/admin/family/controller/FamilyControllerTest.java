package hotspot.admin.family.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import hotspot.admin.family.controller.port.GetFamilyListService;
import hotspot.admin.family.controller.port.GetFamilyRequestListService;
import hotspot.admin.family.controller.port.GetFamilySummaryService;
import hotspot.admin.family.controller.port.ProcessFamilyRequestService;
import hotspot.admin.family.controller.port.SearchFamilyByPhoneService;
import hotspot.admin.family.controller.response.FamilyListItem;
import hotspot.admin.family.controller.response.FamilyListResponse;
import hotspot.admin.family.controller.response.FamilyPhoneSearchResponse;
import hotspot.admin.family.controller.response.FamilyRequestListItem;
import hotspot.admin.family.controller.response.FamilyRequestListResponse;
import hotspot.admin.family.controller.response.FamilySummaryResponse;
import hotspot.admin.family.domain.ApplyType;
import hotspot.admin.family.domain.FamilyApplyStatus;
import hotspot.admin.family.domain.FamilyRole;

@WebMvcTest(FamilyController.class)
@AutoConfigureMockMvc(addFilters = false)
class FamilyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetFamilyListService getFamilyListService;

    @MockBean
    private GetFamilySummaryService getFamilySummaryService;

    @MockBean
    private SearchFamilyByPhoneService searchFamilyByPhoneService;

    @MockBean
    private GetFamilyRequestListService getFamilyRequestListService;

    @MockBean
    private ProcessFamilyRequestService processFamilyRequestService;

    @Test
    @DisplayName("가족 목록 조회 성공")
    void getFamilyListSuccess() throws Exception {
        FamilyListItem item = FamilyListItem.builder()
                .familyId(1L)
                .representativeName("강주민")
                .phoneNumber("010-****-0000")
                .memberCount(3)
                .build();

        FamilyListResponse response = FamilyListResponse.builder()
                .page(0)
                .size(20)
                .totalElements(1L)
                .totalPages(1)
                .hasNext(false)
                .familyList(List.of(item))
                .build();

        when(getFamilyListService.getFamilyList(any()))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/families")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andExpect(jsonPath("$.data.familyList[0].familyId").value(1))
                .andExpect(jsonPath("$.data.familyList[0].phoneNumber").value("010-****-0000"));
    }

    @Test
    @DisplayName("가족 상세 상단 조회 성공")
    void getFamilySummarySuccess() throws Exception {
        FamilySummaryResponse response = FamilySummaryResponse.builder()
                .familyId(9L)
                .displayId("FAM-000009")
                .representativeName("김대표")
                .phoneNumber("010-****-5678")
                .memberCount(5)
                .build();

        when(getFamilySummaryService.getFamilySummary(9L))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/families/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.familyId").value(9))
                .andExpect(jsonPath("$.data.displayId").value("FAM-000009"))
                .andExpect(jsonPath("$.data.representativeName").value("김대표"))
                .andExpect(jsonPath("$.data.phoneNumber").value("010-****-5678"))
                .andExpect(jsonPath("$.data.memberCount").value(5));
    }

    @Test
    @DisplayName("전화번호 검색 성공")
    void searchFamilyByPhoneSuccess() throws Exception {
        FamilyListItem item = FamilyListItem.builder()
                .familyId(2L)
                .representativeName("김보호자")
                .phoneNumber("010-****-1234")
                .memberCount(4)
                .build();

        FamilyPhoneSearchResponse response = FamilyPhoneSearchResponse.builder()
                .family(item)
                .build();

        when(searchFamilyByPhoneService.searchByPhone("010-1234-1234"))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/families/search/phone")
                        .param("phoneNumber", "010-1234-1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.family.familyId").value(2))
                .andExpect(jsonPath("$.data.family.phoneNumber").value("010-****-1234"));
    }

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
                .targetName("추가대상")
                .targetPhoneNumber("010-****-2222")
                .targetFamilyRole(FamilyRole.CHILD)
                .relationDocumentUrl("https://doc.example/1")
                .requestedAt(java.time.LocalDateTime.of(2026, 2, 24, 9, 30))
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

        when(getFamilyRequestListService.getFamilyRequests(
                org.mockito.ArgumentMatchers.eq(ApplyType.ADD),
                org.mockito.ArgumentMatchers.eq(FamilyApplyStatus.PENDING),
                any()
        ))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/families/requests/add/pending")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andExpect(jsonPath("$.data.applyType").value("ADD"))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.requests[0].requestDisplayId").value("REQ-013"))
                .andExpect(jsonPath("$.data.requests[0].requesterName").value("가족대표"));
    }

    @Test
    @DisplayName("가족 요청 승인 성공")
    void approveFamilyRequestSuccess() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .patch("/api/v1/admin/families/requests/remove/99/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("가족 요청 거절 성공")
    void rejectFamilyRequestSuccess() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .patch("/api/v1/admin/families/requests/add/100/reject"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());
    }
}
