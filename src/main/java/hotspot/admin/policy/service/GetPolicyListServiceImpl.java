package hotspot.admin.policy.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.admin.appservice.domain.AppBlockedService;
import hotspot.admin.common.domain.DisplayIdType;
import hotspot.admin.common.util.DisplayIdFormatter;
import hotspot.admin.policy.controller.port.GetPolicyListService;
import hotspot.admin.policy.controller.request.PolicyListRequest;
import hotspot.admin.policy.controller.response.AppPolicyListItem;
import hotspot.admin.policy.controller.response.AppPolicyListResponse;
import hotspot.admin.policy.controller.response.TimePolicyListItem;
import hotspot.admin.policy.controller.response.TimePolicyListResponse;
import hotspot.admin.policy.domain.BlockPolicy;
import hotspot.admin.policy.service.port.AppBlockedServiceRepository;
import hotspot.admin.policy.service.port.BlockPolicyRepository;
import hotspot.admin.policy.util.PolicyScheduleLabelFormatter;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetPolicyListServiceImpl implements GetPolicyListService {

    private final BlockPolicyRepository blockPolicyRepository;
    private final AppBlockedServiceRepository appBlockedServiceRepository;

    @Transactional(readOnly = true)
    @Override
    public TimePolicyListResponse getTimePolicies(PolicyListRequest request) {
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(Sort.Direction.ASC, "blockPolicyId")
        );

        Page<BlockPolicy> result = blockPolicyRepository.findAll(pageable);
        return TimePolicyListResponse.builder()
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .hasNext(result.hasNext())
                .items(result.getContent().stream()
                        .map(this::toTimeItem)
                        .toList())
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public AppPolicyListResponse getAppPolicies(PolicyListRequest request) {
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(Sort.Direction.ASC, "appBlockedServiceId")
        );

        Page<AppBlockedService> result = appBlockedServiceRepository.findAll(pageable);
        return AppPolicyListResponse.builder()
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .hasNext(result.hasNext())
                .items(result.getContent().stream()
                        .map(this::toAppItem)
                        .toList())
                .build();
    }

    private TimePolicyListItem toTimeItem(BlockPolicy blockPolicy) {
        boolean active = Boolean.TRUE.equals(blockPolicy.getIsActive());
        return TimePolicyListItem.builder()
                .policyId(blockPolicy.getBlockPolicyId())
                .displayId(DisplayIdFormatter.format(DisplayIdType.TIME_POLICY, blockPolicy.getBlockPolicyId()))
                .policyName(blockPolicy.getPolicyName())
                .policyDescription(blockPolicy.getPolicyDescription())
                .policyType(blockPolicy.getPolicyType())
                .policyScheduleLabel(
                        PolicyScheduleLabelFormatter.toPolicyScheduleLabel(blockPolicy.getPolicySnapshot())
                )
                .isActive(active)
                .createdTime(blockPolicy.getCreatedTime())
                .build();
    }

    private AppPolicyListItem toAppItem(AppBlockedService appBlockedService) {
        boolean active = Boolean.TRUE.equals(appBlockedService.getIsActive());
        return AppPolicyListItem.builder()
                .policyId(appBlockedService.getAppBlockedServiceId())
                .displayId(DisplayIdFormatter.format(
                        DisplayIdType.APP_POLICY,
                        appBlockedService.getAppBlockedServiceId()
                ))
                .policyName(appBlockedService.getBlockedServiceName())
                .policyCode(appBlockedService.getBlockedServiceCode())
                .isActive(active)
                .createdTime(appBlockedService.getCreatedTime())
                .build();
    }
}
