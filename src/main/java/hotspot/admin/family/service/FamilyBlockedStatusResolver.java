package hotspot.admin.family.service;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import hotspot.admin.family.infrastructure.query.dto.FamilyPolicyMemberRow;
import hotspot.admin.family.infrastructure.query.dto.FamilyPolicyTimeOptionRow;
import hotspot.admin.family.infrastructure.query.dto.FamilyPolicyTimePolicyRow;
import hotspot.admin.family.service.port.FamilyPolicyAssignmentRepository;
import hotspot.admin.family.service.port.FamilySubQueryRepository;
import hotspot.admin.policy.domain.PolicyDay;
import hotspot.admin.policy.domain.PolicySnapshot;
import hotspot.admin.policy.domain.PolicyType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class FamilyBlockedStatusResolver {

    private final FamilySubQueryRepository familySubQueryRepository;
    private final FamilyPolicyAssignmentRepository familyPolicyAssignmentRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Map<Long, Boolean> resolveBlockedBySubId(Long familyId) {
        List<FamilyPolicyMemberRow> members = familySubQueryRepository.findFamilyPolicyMembers(familyId);
        List<FamilyPolicyTimePolicyRow> memberTimePolicies = familySubQueryRepository.findFamilyTimePolicies(familyId);
        Map<Long, FamilyPolicyTimeOptionRow> timePolicyOptionById = familySubQueryRepository
                .findFamilyTimePolicyOptions(familyId).stream()
                .collect(Collectors.toMap(
                        FamilyPolicyTimeOptionRow::policyId,
                        row -> row,
                        (existing, replacement) -> existing
                ));

        LocalDateTime now = LocalDateTime.now(clock);
        Set<Long> deactivatedPolicySubIds = deactivateExpiredOncePolicies(
                memberTimePolicies,
                timePolicyOptionById,
                now
        );

        Map<Long, PolicySnapshot> policySnapshotById = timePolicyOptionById.values().stream()
                .collect(Collectors.toMap(
                        FamilyPolicyTimeOptionRow::policyId,
                        row -> parsePolicySnapshot(row.policySnapshotJson()),
                        (existing, replacement) -> existing
                ));

        Set<Long> timePolicyBlockedSubIds = memberTimePolicies.stream()
                .filter(row -> !deactivatedPolicySubIds.contains(row.policySubId()))
                .filter(row -> isTimePolicyBlockingNow(policySnapshotById.get(row.policyId()), now))
                .map(FamilyPolicyTimePolicyRow::subId)
                .collect(Collectors.toSet());

        return members.stream()
                .collect(Collectors.toMap(
                        FamilyPolicyMemberRow::subId,
                        member -> Boolean.TRUE.equals(member.blocked())
                                || timePolicyBlockedSubIds.contains(member.subId()),
                        (existing, replacement) -> existing
                ));
    }

    private Set<Long> deactivateExpiredOncePolicies(
            List<FamilyPolicyTimePolicyRow> memberTimePolicies,
            Map<Long, FamilyPolicyTimeOptionRow> timePolicyOptionById,
            LocalDateTime now
    ) {
        Set<Long> expiredPolicySubIds = memberTimePolicies.stream()
                .filter(row -> isOncePolicyExpired(row, timePolicyOptionById, now))
                .map(FamilyPolicyTimePolicyRow::policySubId)
                .collect(Collectors.toSet());

        familyPolicyAssignmentRepository.bulkDeactivateTimePoliciesByIds(expiredPolicySubIds);
        return expiredPolicySubIds;
    }

    private boolean isOncePolicyExpired(
            FamilyPolicyTimePolicyRow row,
            Map<Long, FamilyPolicyTimeOptionRow> timePolicyOptionById,
            LocalDateTime now
    ) {
        FamilyPolicyTimeOptionRow option = timePolicyOptionById.get(row.policyId());
        if (option == null || option.policyType() != PolicyType.ONCE) {
            return false;
        }

        PolicySnapshot snapshot = parsePolicySnapshot(option.policySnapshotJson());
        if (snapshot == null || row.modifiedTime() == null) {
            return false;
        }

        Integer durationMinutes = snapshot.getDurationMinutes();
        if (durationMinutes != null && durationMinutes > 0) {
            return now.isAfter(row.modifiedTime().plusMinutes(durationMinutes));
        }

        LocalTime start = snapshot.getStartLocalTime();
        LocalTime end = snapshot.getEndLocalTime();
        if (start == null || end == null) {
            return false;
        }

        LocalDateTime expirationTime = row.modifiedTime().toLocalDate().atTime(end);
        if (end.isBefore(start)) {
            expirationTime = expirationTime.plusDays(1);
        }
        return now.isAfter(expirationTime);
    }

    static boolean isTimePolicyBlockingNow(PolicySnapshot snapshot, LocalDateTime now) {
        if (snapshot == null) {
            return false;
        }

        LocalTime start = snapshot.getStartLocalTime();
        LocalTime end = snapshot.getEndLocalTime();
        if (start == null || end == null) {
            return false;
        }

        Set<PolicyDay> days = toDaySet(snapshot.getDays());
        PolicyDay today = toPolicyDay(now.getDayOfWeek());
        PolicyDay yesterday = previousDay(today);
        LocalTime currentTime = now.toLocalTime();

        if (start.equals(end)) {
            return days == null || days.contains(today);
        }

        boolean overnight = start.isAfter(end);
        if (!overnight) {
            boolean inRange = !currentTime.isBefore(start) && currentTime.isBefore(end);
            return inRange && (days == null || days.contains(today));
        }

        boolean inLateRange = !currentTime.isBefore(start);
        if (inLateRange) {
            return days == null || days.contains(today);
        }

        boolean inEarlyRange = currentTime.isBefore(end);
        if (inEarlyRange) {
            return days == null || days.contains(yesterday);
        }

        return false;
    }

    private PolicySnapshot parsePolicySnapshot(String policySnapshotJson) {
        if (policySnapshotJson == null || policySnapshotJson.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readValue(policySnapshotJson, PolicySnapshot.class);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse PolicySnapshot JSON: {}", policySnapshotJson, e);
            return null;
        }
    }

    private static Set<PolicyDay> toDaySet(List<PolicyDay> days) {
        if (days == null || days.isEmpty()) {
            return null;
        }
        return EnumSet.copyOf(days);
    }

    private static PolicyDay toPolicyDay(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> PolicyDay.MONDAY;
            case TUESDAY -> PolicyDay.TUESDAY;
            case WEDNESDAY -> PolicyDay.WEDNESDAY;
            case THURSDAY -> PolicyDay.THURSDAY;
            case FRIDAY -> PolicyDay.FRIDAY;
            case SATURDAY -> PolicyDay.SATURDAY;
            case SUNDAY -> PolicyDay.SUNDAY;
        };
    }

    private static PolicyDay previousDay(PolicyDay day) {
        return switch (day) {
            case MONDAY -> PolicyDay.SUNDAY;
            case TUESDAY -> PolicyDay.MONDAY;
            case WEDNESDAY -> PolicyDay.TUESDAY;
            case THURSDAY -> PolicyDay.WEDNESDAY;
            case FRIDAY -> PolicyDay.THURSDAY;
            case SATURDAY -> PolicyDay.FRIDAY;
            case SUNDAY -> PolicyDay.SATURDAY;
        };
    }
}
