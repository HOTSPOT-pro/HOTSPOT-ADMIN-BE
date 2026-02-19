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
import hotspot.admin.family.controller.response.FamilyListItem;
import hotspot.admin.family.controller.response.FamilyListResponse;

@WebMvcTest(FamilyController.class)
@AutoConfigureMockMvc(addFilters = false)
class FamilyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetFamilyListService getFamilyListService;

    @Test
    @DisplayName("가족 목록 조회 성공")
    void getFamilyListSuccess() throws Exception {
        FamilyListItem item = FamilyListItem.builder()
                .familyId(1L)
                .representativeName("강주민")
                .phoneNumber("010-****-0000")
                .memberCount(3)
                .usedData(null)
                .remainingData(null)
                .build();

        FamilyListResponse response = FamilyListResponse.builder()
                .size(30)
                .nextCursor(30L)
                .hasNext(true)
                .familyList(List.of(item))
                .build();

        when(getFamilyListService.getFamilyList(any()))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/families")
                        .param("size", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size").value(30))
                .andExpect(jsonPath("$.data.nextCursor").value(30))
                .andExpect(jsonPath("$.data.hasNext").value(true))
                .andExpect(jsonPath("$.data.familyList[0].familyId").value(1))
                .andExpect(jsonPath("$.data.familyList[0].phoneNumber").value("010-****-0000"));
    }
}
