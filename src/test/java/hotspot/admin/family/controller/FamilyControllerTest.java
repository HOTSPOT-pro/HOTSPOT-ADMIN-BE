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

import hotspot.admin.family.controller.port.GetFamilyControlStatusService;
import hotspot.admin.family.controller.port.GetFamilyListService;
import hotspot.admin.family.controller.port.GetFamilyPolicyDetailStatusService;
import hotspot.admin.family.controller.port.GetFamilyPolicyStatusService;
import hotspot.admin.family.controller.port.GetFamilySummaryService;
import hotspot.admin.family.controller.port.SearchFamilyByPhoneService;
import hotspot.admin.family.controller.response.FamilyControlMemberItem;
import hotspot.admin.family.controller.response.FamilyControlStatusResponse;
import hotspot.admin.family.controller.response.FamilyListItem;
import hotspot.admin.family.controller.response.FamilyListResponse;
import hotspot.admin.family.controller.response.FamilyPhoneSearchResponse;
import hotspot.admin.family.controller.response.FamilyPolicyAppItem;
import hotspot.admin.family.controller.response.FamilyPolicyMemberStatusItem;
import hotspot.admin.family.controller.response.FamilyPolicyTimeItem;
import hotspot.admin.family.controller.response.FamilySummaryResponse;
import hotspot.admin.family.domain.FamilyRole;
import hotspot.admin.family.domain.PriorityType;

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
    private GetFamilyControlStatusService getFamilyControlStatusService;

    @MockBean
    private GetFamilyPolicyStatusService getFamilyPolicyStatusService;

    @MockBean
    private GetFamilyPolicyDetailStatusService getFamilyPolicyDetailStatusService;

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
    @DisplayName("가족 제어 기능 조회 성공")
    void getFamilyControlStatusSuccess() throws Exception {
        FamilyControlStatusResponse response = FamilyControlStatusResponse.builder()
                .priorityType(PriorityType.FIFO)
                .members(List.of(
                        FamilyControlMemberItem.builder()
                                .subId(101L)
                                .memberName("홍대표")
                                .familyRole(FamilyRole.OWNER)
                                .blocked(false)
                                .dataLimitGb(0.0009765625D)
                                .priorityOrder(-1)
                                .build(),
                        FamilyControlMemberItem.builder()
                                .subId(102L)
                                .memberName("홍부모")
                                .familyRole(FamilyRole.PARENT)
                                .blocked(true)
                                .dataLimitGb(0.00048828125D)
                                .priorityOrder(-1)
                                .build()
                ))
                .build();

        when(getFamilyControlStatusService.getFamilyControlStatus(5L))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/families/5/control-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.priorityType").value("FIFO"))
                .andExpect(jsonPath("$.data.members[0].subId").value(101))
                .andExpect(jsonPath("$.data.members[0].memberName").value("홍대표"))
                .andExpect(jsonPath("$.data.members[0].familyRole").value("OWNER"))
                .andExpect(jsonPath("$.data.members[0].priorityOrder").value(-1))
                .andExpect(jsonPath("$.data.members[1].familyRole").value("PARENT"))
                .andExpect(jsonPath("$.data.members[1].blocked").value(true));
    }

    @Test
    @DisplayName("가족 정책 적용 현황 조회 성공")
    void getFamilyPolicyStatusSuccess() throws Exception {
        FamilyPolicyMemberStatusItem member = FamilyPolicyMemberStatusItem.builder()
                .subId(101L)
                .memberName("홍길동")
                .phoneNumber("010-****-1111")
                .familyRole(FamilyRole.OWNER)
                .blocked(true)
                .appliedTimePolicies(List.of("야간 차단"))
                .appliedBlockedServicePolicies(List.of("유튜브", "틱톡"))
                .build();

        when(getFamilyPolicyStatusService.getFamilyPolicyStatus(5L))
                .thenReturn(List.of(member));

        mockMvc.perform(get("/api/v1/admin/families/5/policy-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].subId").value(101))
                .andExpect(jsonPath("$.data[0].memberName").value("홍길동"))
                .andExpect(jsonPath("$.data[0].familyRole").value("OWNER"))
                .andExpect(jsonPath("$.data[0].blocked").value(true))
                .andExpect(jsonPath("$.data[0].appliedTimePolicies[0]").value("야간 차단"))
                .andExpect(jsonPath("$.data[0].appliedBlockedServicePolicies[0]").value("유튜브"));
    }

    @Test
    @DisplayName("가족 정책 상세 조회 성공")
    void getFamilyPolicyDetailStatusSuccess() throws Exception {
        hotspot.admin.family.controller.response.FamilyPolicyMemberDetailItem member = hotspot.admin.family.controller
                .response.FamilyPolicyMemberDetailItem.builder()
                .memberName("홍길동")
                .phoneNumber("010-****-1111")
                .familyRole(FamilyRole.OWNER)
                .blocked(true)
                .appliedTimePolicies(List.of(
                        FamilyPolicyTimeItem.builder()
                                .policyId(1L)
                                .policyName("야간 차단")
                                .policyDescription("매일 야간 차단")
                                .policyType(hotspot.admin.policy.domain.PolicyType.SCHEDULED)
                                .policyScheduleLabel("주중 22:00~07:00")
                                .isActive(true)
                                .build()
                ))
                .appliedBlockedServicePolicies(List.of(
                        FamilyPolicyAppItem.builder()
                                .policyId(11L)
                                .policyName("유튜브")
                                .isActive(true)
                                .build()
                ))
                .build();

        when(getFamilyPolicyDetailStatusService.getFamilyPolicyDetailStatus(5L, 101L))
                .thenReturn(member);

        mockMvc.perform(get("/api/v1/admin/families/5/members/101/policy-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.memberName").value("홍길동"))
                .andExpect(jsonPath("$.data.appliedTimePolicies[0].policyName").value("야간 차단"))
                .andExpect(jsonPath("$.data.appliedTimePolicies[0].policyType").value("SCHEDULED"))
                .andExpect(jsonPath("$.data.appliedTimePolicies[0].policyScheduleLabel").value("주중 22:00~07:00"))
                .andExpect(jsonPath("$.data.appliedTimePolicies[0].isActive").value(true));
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
