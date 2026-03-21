package hotspot.admin.family.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.code.FamilyErrorCode;
import hotspot.admin.common.util.redis.RedisUsageCalculator;
import hotspot.admin.family.controller.port.GetFamilyControlStatusService;
import hotspot.admin.family.controller.response.FamilyControlMemberItem;
import hotspot.admin.family.controller.response.FamilyControlStatusResponse;
import hotspot.admin.family.domain.FamilyRole;
import hotspot.admin.family.domain.PriorityType;
import hotspot.admin.family.infrastructure.query.dto.FamilyControlMemberRow;
import hotspot.admin.family.infrastructure.schema.FamilyDataControl;
import hotspot.admin.family.service.port.FamilyDataLimitRepository;
import hotspot.admin.family.service.port.FamilyRepository;
import hotspot.admin.family.service.port.FamilySubQueryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetFamilyControlStatusServiceImpl implements GetFamilyControlStatusService {

    private static final int UNUSED_PRIORITY_ORDER = -1;

    private final FamilyRepository familyRepository;
    private final FamilySubQueryRepository familySubQueryRepository;
    private final FamilyDataLimitRepository familyDataLimitRepository;

    /** 가족 우선순위 유형과 구성원별 제어 상태를 제어 탭 응답 형태로 변환한다. */
    @Transactional(readOnly = true)
    @Override
    public FamilyControlStatusResponse getFamilyControlStatus(Long familyId) {
        PriorityType priorityType = familyRepository.findFamilyPriorityType(familyId)
                .orElseThrow(() -> new ApplicationException(FamilyErrorCode.FAMILY_NOT_FOUND));

        // DB 조회
        List<FamilyControlMemberRow> memberRows =
                familySubQueryRepository.findFamilyControlMembers(familyId);

        // Redis 조회
        FamilyDataControl familyData =
                familyDataLimitRepository.findFamilyDataLimit(familyId);

        // Redis subId → usage map 생성
        Map<Long, FamilyDataControl.SubFamilyDataControl> dataMap =
                familyData.subFamilies().stream()
                        .collect(Collectors.toMap(
                                FamilyDataControl.SubFamilyDataControl::subId,
                                Function.identity()
                        ));

        // 최종 매핑
        List<FamilyControlMemberItem> members = memberRows.stream()
                .map(member -> toMemberItem(member, priorityType, familyData.familyDataLimit(), dataMap))
                .toList();

        return FamilyControlStatusResponse.builder()
                .priorityType(priorityType)
                .members(members)
                .build();
    }

    /** 구성원 제어 조회 행을 제어 탭 응답 항목으로 변환한다. */
    private FamilyControlMemberItem toMemberItem(
            FamilyControlMemberRow member,
            PriorityType priorityType,
            Long familyLimit,
            Map<Long, FamilyDataControl.SubFamilyDataControl> dataMap
    ) {

        FamilyDataControl.SubFamilyDataControl data =
                dataMap.get(member.subId());

        Long usage = data != null ? data.familyDataUsage() : 0L;

        double subLimit = RedisUsageCalculator.kbToGb(member.dataLimit());

        return FamilyControlMemberItem.builder()
                .subId(member.subId())
                .memberName(member.memberName())
                .familyRole(member.familyRole())
                .isParent(resolveIsParent(member.familyRole()))
                .isBlocked(Boolean.TRUE.equals(member.blocked()))
                .familyDataLimit(familyLimit)
                .familyDataUsage(usage)
                .familyDataSubLimit((long) subLimit)
                .priorityOrder(resolvePriorityOrder(member.priority(), priorityType))
                .build();
    }

    private Boolean resolveIsParent(FamilyRole familyRole) {
        return switch (familyRole) {
            case PARENT -> true;
            case CHILD -> false;
            case OWNER -> null;
        };
    }

    /** 우선순위 유형 규칙에 따라 화면 노출용 우선순위 순서를 계산한다. */
    private Integer resolvePriorityOrder(Integer priority, PriorityType priorityType) {
        if (priorityType == PriorityType.FIFO) {
            return UNUSED_PRIORITY_ORDER;
        }
        return priority == null ? UNUSED_PRIORITY_ORDER : priority;
    }
}
