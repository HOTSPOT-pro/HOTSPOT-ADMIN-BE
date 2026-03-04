package hotspot.admin.family.service;

import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.code.FamilyErrorCode;
import hotspot.admin.common.util.PhoneCryptoUtil;
import hotspot.admin.common.util.PhoneMaskingUtil;
import hotspot.admin.family.controller.port.GetFamilyPolicyDetailStatusService;
import hotspot.admin.family.controller.response.FamilyPolicyAppItem;
import hotspot.admin.family.controller.response.FamilyPolicyMemberDetailItem;
import hotspot.admin.family.controller.response.FamilyPolicyTimeItem;
import hotspot.admin.family.infrastructure.query.dto.FamilyPolicyAppOptionRow;
import hotspot.admin.family.infrastructure.query.dto.FamilyPolicyMemberRow;
import hotspot.admin.family.infrastructure.query.dto.FamilyPolicyTimeOptionRow;
import hotspot.admin.family.service.port.FamilyRepository;
import hotspot.admin.family.service.port.FamilySubQueryRepository;
import hotspot.admin.policy.domain.PolicyDay;
import hotspot.admin.policy.domain.PolicySnapshot;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetFamilyPolicyDetailStatusServiceImpl implements GetFamilyPolicyDetailStatusService {

    private static final Set<PolicyDay> WEEKDAYS = Set.of(
            PolicyDay.MON,
            PolicyDay.TUE,
            PolicyDay.WED,
            PolicyDay.THU,
            PolicyDay.FRI
    );
    private static final Set<PolicyDay> WEEKEND = Set.of(PolicyDay.SAT, PolicyDay.SUN);
    private static final Set<PolicyDay> EVERYDAY = Set.of(PolicyDay.values());

    private final FamilyRepository familyRepository;
    private final FamilySubQueryRepository familySubQueryRepository;
    private final PhoneCryptoUtil phoneCryptoUtil;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    @Override
    public FamilyPolicyMemberDetailItem getFamilyPolicyDetailStatus(Long familyId, Long subId) {
        if (!familyRepository.existsFamilyById(familyId)) {
            throw new ApplicationException(FamilyErrorCode.FAMILY_NOT_FOUND);
        }

        List<FamilyPolicyMemberRow> members = familySubQueryRepository.findFamilyPolicyMembers(familyId);
        List<FamilyPolicyTimeOptionRow> timePolicyOptions = familySubQueryRepository
                .findFamilyTimePolicyOptions(familyId);
        List<FamilyPolicyAppOptionRow> appPolicyOptions = familySubQueryRepository.findFamilyAppPolicyOptions(familyId);

        Map<Long, Set<Long>> appliedTimePolicyIds = familySubQueryRepository.findFamilyTimePolicies(familyId).stream()
                .collect(Collectors.groupingBy(
                        row -> row.subId(),
                        Collectors.mapping(row -> row.policyId(), Collectors.toSet())
                ));

        Map<Long, Set<Long>> appliedAppPolicyIds = familySubQueryRepository.findFamilyAppPolicies(familyId).stream()
                .collect(Collectors.groupingBy(
                        row -> row.subId(),
                        Collectors.mapping(row -> row.policyId(), Collectors.toSet())
                ));

        FamilyPolicyMemberRow member = members.stream()
                .filter(item -> item.subId().equals(subId))
                .findFirst()
                .orElseThrow(() -> new ApplicationException(FamilyErrorCode.FAMILY_MEMBER_NOT_FOUND));

        return toMemberItem(
                member,
                timePolicyOptions,
                appPolicyOptions,
                appliedTimePolicyIds,
                appliedAppPolicyIds
        );
    }

    private FamilyPolicyMemberDetailItem toMemberItem(
            FamilyPolicyMemberRow member,
            List<FamilyPolicyTimeOptionRow> timePolicyOptions,
            List<FamilyPolicyAppOptionRow> appPolicyOptions,
            Map<Long, Set<Long>> appliedTimePolicyIds,
            Map<Long, Set<Long>> appliedAppPolicyIds
    ) {
        Set<Long> memberTimePolicyIds = appliedTimePolicyIds.getOrDefault(member.subId(), Set.of());
        Set<Long> memberAppPolicyIds = appliedAppPolicyIds.getOrDefault(member.subId(), Set.of());

        return FamilyPolicyMemberDetailItem.builder()
                .memberName(member.memberName())
                .phoneNumber(decryptAndMaskPhone(member.phoneNumberEnc()))
                .familyRole(member.familyRole())
                .blocked(Boolean.TRUE.equals(member.blocked()))
                .appliedTimePolicies(toTimeItems(timePolicyOptions, memberTimePolicyIds))
                .appliedBlockedServicePolicies(toAppItems(appPolicyOptions, memberAppPolicyIds))
                .build();
    }

    private List<FamilyPolicyTimeItem> toTimeItems(
            List<FamilyPolicyTimeOptionRow> options,
            Set<Long> appliedPolicyIds
    ) {
        return options.stream()
                .map(option -> FamilyPolicyTimeItem.builder()
                        .policyId(option.policyId())
                        .policyName(option.policyName())
                        .policyDescription(option.policyDescription())
                        .policyType(option.policyType())
                        .policyScheduleLabel(toPolicyScheduleLabel(parsePolicySnapshot(option.policySnapshotJson())))
                        .isActive(appliedPolicyIds.contains(option.policyId()))
                        .build())
                .toList();
    }

    private List<FamilyPolicyAppItem> toAppItems(
            List<FamilyPolicyAppOptionRow> options,
            Set<Long> appliedPolicyIds
    ) {
        return options.stream()
                .map(option -> FamilyPolicyAppItem.builder()
                        .policyId(option.policyId())
                        .policyName(option.policyName())
                        .isActive(appliedPolicyIds.contains(option.policyId()))
                        .build())
                .toList();
    }

    private PolicySnapshot parsePolicySnapshot(String policySnapshotJson) {
        if (policySnapshotJson == null || policySnapshotJson.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(policySnapshotJson, PolicySnapshot.class);
        } catch (JsonProcessingException e) {
            return null;
        }
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

        List<PolicyDay> days = snapshot.getDays();
        if (days == null || days.isEmpty()) {
            return startTime + "~" + endTime;
        }

        Set<PolicyDay> daySet = Set.copyOf(days);
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

    private String toKoreanDayShort(PolicyDay day) {
        return switch (day) {
            case MON -> "월";
            case TUE -> "화";
            case WED -> "수";
            case THU -> "목";
            case FRI -> "금";
            case SAT -> "토";
            case SUN -> "일";
        };
    }

    private String decryptAndMaskPhone(String encryptedPhone) {
        if (encryptedPhone == null || encryptedPhone.isBlank()) {
            return encryptedPhone;
        }

        try {
            String decrypted = phoneCryptoUtil.decryptPhone(encryptedPhone);
            return PhoneMaskingUtil.maskMiddle(decrypted);
        } catch (GeneralSecurityException e) {
            throw new ApplicationException(FamilyErrorCode.PHONE_DECRYPT_FAILED);
        }
    }
}
