package hotspot.admin.family.service;

import java.security.GeneralSecurityException;
import java.util.Comparator;
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
import hotspot.admin.policy.domain.PolicySnapshot;
import hotspot.admin.policy.util.PolicyScheduleLabelFormatter;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetFamilyPolicyDetailStatusServiceImpl implements GetFamilyPolicyDetailStatusService {

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
        List<FamilyPolicyAppOptionRow> appPolicyOptions = familySubQueryRepository.findAllAppPolicyOptions();

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
                        .policyScheduleLabel(PolicyScheduleLabelFormatter.toPolicyScheduleLabel(
                                parsePolicySnapshot(option.policySnapshotJson())
                        ))
                        .isActive(appliedPolicyIds.contains(option.policyId()))
                        .build())
                .sorted(Comparator
                        .comparing((FamilyPolicyTimeItem item) -> Boolean.TRUE.equals(item.isActive()))
                        .reversed()
                        .thenComparing(
                                FamilyPolicyTimeItem::policyName,
                                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
                        )
                        .thenComparing(FamilyPolicyTimeItem::policyId, Comparator.nullsLast(Long::compareTo)))
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
                .sorted(Comparator
                        .comparing((FamilyPolicyAppItem item) -> Boolean.TRUE.equals(item.isActive()))
                        .reversed()
                        .thenComparing(
                                FamilyPolicyAppItem::policyName,
                                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
                        )
                        .thenComparing(FamilyPolicyAppItem::policyId, Comparator.nullsLast(Long::compareTo)))
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
