package hotspot.admin.policy.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import hotspot.admin.policy.controller.port.CreatePolicyService;
import hotspot.admin.policy.controller.port.DeletePolicyService;
import hotspot.admin.policy.controller.port.GetPolicyListService;
import hotspot.admin.policy.controller.port.UpdatePolicyActiveService;
import hotspot.admin.policy.controller.response.AppPolicyListItem;
import hotspot.admin.policy.controller.response.AppPolicyListResponse;
import hotspot.admin.policy.controller.response.CreateAppPolicyResponse;
import hotspot.admin.policy.controller.response.CreateTimePolicyResponse;
import hotspot.admin.policy.controller.response.TimePolicyListItem;
import hotspot.admin.policy.controller.response.TimePolicyListResponse;
import hotspot.admin.policy.controller.response.UpdatePolicyActiveResponse;
import hotspot.admin.policy.domain.PolicyType;

@WebMvcTest(PolicyController.class)
@AutoConfigureMockMvc(addFilters = false)
class PolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetPolicyListService getPolicyListService;

    @MockBean
    private CreatePolicyService createPolicyService;

    @MockBean
    private UpdatePolicyActiveService updatePolicyActiveService;

    @MockBean
    private DeletePolicyService deletePolicyService;

    @Test
    @DisplayName("시간 정책 목록 조회 성공")
    void getTimePoliciesSuccess() throws Exception {
        TimePolicyListResponse response = TimePolicyListResponse.builder()
                .page(0)
                .size(20)
                .totalElements(1L)
                .totalPages(1)
                .hasNext(false)
                .items(List.of(TimePolicyListItem.builder()
                        .policyId(1L)
                        .displayId("TP-001")
                        .policyName("수면모드")
                        .policyType(PolicyType.SCHEDULED)
                        .policyScheduleLabel("매일 00:00~07:00")
                        .isActive(true)
                        .build()))
                .build();
        when(getPolicyListService.getTimePolicies(any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/policies/time").param("page", "0").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].displayId").value("TP-001"))
                .andExpect(jsonPath("$.data.items[0].is_active").value(true));
    }

    @Test
    @DisplayName("앱 정책 목록 조회 성공")
    void getAppPoliciesSuccess() throws Exception {
        AppPolicyListResponse response = AppPolicyListResponse.builder()
                .page(0)
                .size(20)
                .totalElements(1L)
                .totalPages(1)
                .hasNext(false)
                .items(List.of(AppPolicyListItem.builder()
                        .policyId(1L)
                        .displayId("AP-001")
                        .policyName("유튜브")
                        .policyCode("MEDIA_YOUTUBE")
                        .isActive(true)
                        .build()))
                .build();
        when(getPolicyListService.getAppPolicies(any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/policies/app").param("page", "0").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].displayId").value("AP-001"));
    }

    @Test
    @DisplayName("시간 정책 생성 성공")
    void createTimePolicySuccess() throws Exception {
        when(createPolicyService.createTimePolicy(any()))
                .thenReturn(CreateTimePolicyResponse.builder()
                        .policyId(2L)
                        .displayId("TP-002")
                        .isActive(true)
                        .build());

        String request = """
                {
                  "policyName":"모닝 루틴",
                  "policyType":"SCHEDULED",
                  "policySnapshot":{
                    "days":["MON","TUE"],
                    "startTime":"06:00",
                    "endTime":"07:00"
                  }
                }
                """;

        mockMvc.perform(post("/api/v1/admin/policies/time")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.displayId").value("TP-002"))
                .andExpect(jsonPath("$.data.is_active").value(true));
    }

    @Test
    @DisplayName("시간 정책 생성 실패 - 스냅샷 검증 실패")
    void createTimePolicyBadRequest() throws Exception {
        String request = """
                {
                  "policyName":"시험기간",
                  "policyType":"ONCE",
                  "policySnapshot":{
                    "days":["MON"],
                    "durationMinutes":180
                  }
                }
                """;

        mockMvc.perform(post("/api/v1/admin/policies/time")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("앱 정책 생성 성공")
    void createAppPolicySuccess() throws Exception {
        when(createPolicyService.createAppPolicy(any()))
                .thenReturn(CreateAppPolicyResponse.builder()
                        .policyId(2L)
                        .displayId("AP-002")
                        .isActive(true)
                        .build());

        String request = """
                {
                  "policyName":"유튜브",
                  "policyCode":"MEDIA_YOUTUBE"
                }
                """;

        mockMvc.perform(post("/api/v1/admin/policies/app")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.displayId").value("AP-002"));
    }

    @Test
    @DisplayName("정책 활성화 토글 성공")
    void updatePolicyActiveSuccess() throws Exception {
        when(updatePolicyActiveService.updatePolicyActive(any(), any(), any()))
                .thenReturn(UpdatePolicyActiveResponse.builder()
                        .policyId(1L)
                        .displayId("TP-001")
                        .isActive(false)
                        .build());

        mockMvc.perform(patch("/api/v1/admin/policies/time/1/active")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"isActive": false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.is_active").value(false));
    }

    @Test
    @DisplayName("정책 삭제 성공")
    void deletePolicySuccess() throws Exception {
        doNothing().when(deletePolicyService).deletePolicy(any(), any());

        mockMvc.perform(delete("/api/v1/admin/policies/time/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
    }
}
