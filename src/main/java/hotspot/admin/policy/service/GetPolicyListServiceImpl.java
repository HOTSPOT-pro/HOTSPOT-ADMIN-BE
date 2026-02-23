package hotspot.admin.policy.service;

import java.time.DayOfWeek;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.admin.common.domain.DisplayIdType;
import hotspot.admin.common.util.DisplayIdFormatter;
import hotspot.admin.appservice.domain.AppBlockedService;
import hotspot.admin.policy.controller.port.GetPolicyListService;
import hotspot.admin.policy.controller.request.PolicyListRequest;
import hotspot.admin.policy.controller.response.AppPolicyListItem;
import hotspot.admin.policy.controller.response.AppPolicyListResponse;
import hotspot.admin.policy.controller.response.TimePolicyListItem;
import hotspot.admin.policy.controller.response.TimePolicyListResponse;
import hotspot.admin.policy.domain.BlockPolicy;
import hotspot.admin.policy.domain.PolicySnapshot;
import hotspot.admin.policy.service.port.AppBlockedServiceRepository;
import hotspot.admin.policy.service.port.BlockPolicyRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetPolicyListServiceImpl implements GetPolicyListService {

    private static final Set<DayOfWeek> WEEKDAYS = EnumSet.of(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY
    );

    private static final Set<DayOfWeek> EVERYDAY = EnumSet.allOf(DayOfWeek.class);
    private static final Set<DayOfWeek> WEEKEND = EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

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
                .policyType(blockPolicy.getPolicyType())
                .policyScheduleLabel(toPolicyScheduleLabel(blockPolicy.getPolicySnapshot()))
                .active(active)
                .createdTime(blockPolicy.getCreatedTime())
                .build();
    }

    private AppPolicyListItem toAppItem(AppBlockedService appBlockedService) {
        boolean active = !Boolean.TRUE.equals(appBlockedService.getIsDeleted());
        return AppPolicyListItem.builder()
                .policyId(appBlockedService.getAppBlockedServiceId())
                .displayId(DisplayIdFormatter.format(DisplayIdType.APP_POLICY, appBlockedService.getAppBlockedServiceId()))
                .policyName(appBlockedService.getBlockedServiceName())
                .policyCode(appBlockedService.getBlockedServiceCode())
                .active(active)
                .createdTime(appBlockedService.getCreatedTime())
                .build();
    }

    private String toPolicyScheduleLabel(PolicySnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }

        Integer durationMinutes = snapshot.getDurationMinutes();
        if (durationMinutes != null && durationMinutes > 0) {
            if (durationMinutes % 60 == 0) {
                return (durationMinutes / 60) + "시간";
            }
            return durationMinutes + "분";
        }

        String startTime = snapshot.getStartTime();
        String endTime = snapshot.getEndTime();
        if (startTime == null || endTime == null) {
            return null;
        }

        List<DayOfWeek> days = snapshot.getDays();
        if (days == null || days.isEmpty()) {
            return startTime + "~" + endTime;
        }

        Set<DayOfWeek> daySet = EnumSet.copyOf(days);
        if (daySet.equals(EVERYDAY)) {
            return "매일 " + startTime + "~" + endTime;
        }
        if (daySet.equals(WEEKDAYS)) {
            return "주중 " + startTime + "~" + endTime;
        }
        if (daySet.equals(WEEKEND)) {
            return "주말 " + startTime + "~" + endTime;
        }

        String dayLabel = daySet.stream()
                .sorted()
                .map(this::toKoreanDayShort)
                .collect(Collectors.joining(","));
        return dayLabel + " " + startTime + "~" + endTime;
    }

    private String toKoreanDayShort(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "월";
            case TUESDAY -> "화";
            case WEDNESDAY -> "수";
            case THURSDAY -> "목";
            case FRIDAY -> "금";
            case SATURDAY -> "토";
            case SUNDAY -> "일";
        };
    }
}
