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
import hotspot.admin.family.controller.port.SearchFamilyByPhoneService;
import hotspot.admin.family.controller.response.FamilyListItem;
import hotspot.admin.family.controller.response.FamilyListResponse;
import hotspot.admin.family.controller.response.FamilyPhoneSearchResponse;

@WebMvcTest(FamilyController.class)
@AutoConfigureMockMvc(addFilters = false)
class FamilyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetFamilyListService getFamilyListService;

    @MockBean
    private SearchFamilyByPhoneService searchFamilyByPhoneService;

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
}
