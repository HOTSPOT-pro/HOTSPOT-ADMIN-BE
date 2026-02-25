package hotspot.admin.policy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import hotspot.admin.appservice.domain.AppBlockedService;
import hotspot.admin.policy.controller.request.PolicyListRequest;
import hotspot.admin.policy.controller.response.AppPolicyListResponse;
import hotspot.admin.policy.controller.response.TimePolicyListResponse;
import hotspot.admin.policy.domain.BlockPolicy;
import hotspot.admin.policy.domain.PolicyDay;
import hotspot.admin.policy.domain.PolicySnapshot;
import hotspot.admin.policy.domain.PolicyType;
import hotspot.admin.policy.service.port.AppBlockedServiceRepository;
import hotspot.admin.policy.service.port.BlockPolicyRepository;

@ExtendWith(MockitoExtension.class)
class GetPolicyListServiceImplTest {

    @Mock
    private BlockPolicyRepository blockPolicyRepository;

    @Mock
    private AppBlockedServiceRepository appBlockedServiceRepository;

    private GetPolicyListServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GetPolicyListServiceImpl(blockPolicyRepository, appBlockedServiceRepository);
    }

    @Test
    @DisplayName("시간 정책 목록 조회 및 라벨 변환 성공")
    void getTimePoliciesSuccess() {
        PolicyListRequest request = new PolicyListRequest();
        request.setPage(0);
        request.setSize(20);

        List<BlockPolicy> content = List.of(
                BlockPolicy.builder()
                        .blockPolicyId(1L)
                        .policyName("수면모드")
                        .policyDescription("매일 수면 시간 차단")
                        .policyType(PolicyType.SCHEDULED)
                        .policySnapshot(PolicySnapshot.builder()
                                .days(List.of(
                                        PolicyDay.MON, PolicyDay.TUE, PolicyDay.WED, PolicyDay.THU,
                                        PolicyDay.FRI, PolicyDay.SAT, PolicyDay.SUN
                                ))
                                .startTime("00:00")
                                .endTime("07:00")
                                .build())
                        .isActive(true)
                        .isDeleted(false)
                        .build(),
                BlockPolicy.builder()
                        .blockPolicyId(2L)
                        .policyName("방해 금지")
                        .policyDescription("3시간 집중")
                        .policyType(PolicyType.ONCE)
                        .policySnapshot(PolicySnapshot.builder().durationMinutes(180).build())
                        .isActive(true)
                        .isDeleted(false)
                        .build(),
                BlockPolicy.builder()
                        .blockPolicyId(3L)
                        .policyName("커스텀")
                        .policyDescription("커스텀 요일 차단")
                        .policyType(PolicyType.SCHEDULED)
                        .policySnapshot(PolicySnapshot.builder()
                                .days(List.of(PolicyDay.MON, PolicyDay.WED, PolicyDay.FRI))
                                .startTime("09:00")
                                .endTime("14:00")
                                .build())
                        .isActive(false)
                        .isDeleted(false)
                        .build()
        );

        when(blockPolicyRepository.findAll(any()))
                .thenReturn(new PageImpl<>(content, PageRequest.of(0, 20), content.size()));

        TimePolicyListResponse response = service.getTimePolicies(request);
        assertThat(response.items()).hasSize(3);
        assertThat(response.items().get(0).policyDescription()).isEqualTo("매일 수면 시간 차단");
        assertThat(response.items().get(0).policyScheduleLabel()).isEqualTo("매일 00:00~07:00");
        assertThat(response.items().get(1).policyScheduleLabel()).isEqualTo("3시간");
        assertThat(response.items().get(2).policyScheduleLabel()).isEqualTo("월,수,금 09:00~14:00");
        assertThat(response.items().get(2).isActive()).isFalse();
    }

    @Test
    @DisplayName("앱 정책 목록 조회 성공")
    void getAppPoliciesSuccess() {
        PolicyListRequest request = new PolicyListRequest();
        request.setPage(0);
        request.setSize(20);

        List<AppBlockedService> content = List.of(
                AppBlockedService.builder()
                        .appBlockedServiceId(1L)
                        .blockedServiceName("유튜브")
                        .blockedServiceCode("MEDIA_YOUTUBE")
                        .isActive(true)
                        .isDeleted(false)
                        .build()
        );

        when(appBlockedServiceRepository.findAll(any()))
                .thenReturn(new PageImpl<>(content, PageRequest.of(0, 20), content.size()));

        AppPolicyListResponse response = service.getAppPolicies(request);
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).displayId()).isEqualTo("AP-001");
        assertThat(response.items().get(0).isActive()).isTrue();
    }
}
