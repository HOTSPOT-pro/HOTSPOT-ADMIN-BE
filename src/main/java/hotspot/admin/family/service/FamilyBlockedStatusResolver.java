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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import hotspot.admin.family.infrastructure.query.dto.FamilyPolicyMemberRow;
import hotspot.admin.family.infrastructure.query.dto.FamilyPolicyTimeOptionRow;
import hotspot.admin.family.infrastructure.query.dto.FamilyPolicyTimePolicyRow;
import hotspot.admin.family.service.port.FamilySubQueryRepository;
import hotspot.admin.policy.domain.PolicyDay;
import hotspot.admin.policy.domain.PolicySnapshot;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FamilyBlockedStatusResolver {

    private final FamilySubQueryRepository familySubQueryRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public Map<Long, Boolean> resolveBlockedBySubId(Long familyId) {
        List<FamilyPolicyMemberRow> members = familySubQueryRepository.findFamilyPolicyMembers(familyId);

        Map<Long, PolicySnapshot> policySnapshotById = familySubQueryRepository
                .findFamilyTimePolicyOptions(familyId).stream()
                .collect(Collectors.toMap(
                        FamilyPolicyTimeOptionRow::policyId,
                        row -> parsePolicySnapshot(row.policySnapshotJson()),
                        (existing, replacement) -> existing
                ));

        LocalDateTime now = LocalDateTime.now(clock);
        Set<Long> timePolicyBlockedSubIds = familySubQueryRepository.findFamilyTimePolicies(familyId).stream()
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
            case MONDAY -> PolicyDay.MON;
            case TUESDAY -> PolicyDay.TUE;
            case WEDNESDAY -> PolicyDay.WED;
            case THURSDAY -> PolicyDay.THU;
            case FRIDAY -> PolicyDay.FRI;
            case SATURDAY -> PolicyDay.SAT;
            case SUNDAY -> PolicyDay.SUN;
        };
    }

    private static PolicyDay previousDay(PolicyDay day) {
        return switch (day) {
            case MON -> PolicyDay.SUN;
            case TUE -> PolicyDay.MON;
            case WED -> PolicyDay.TUE;
            case THU -> PolicyDay.WED;
            case FRI -> PolicyDay.THU;
            case SAT -> PolicyDay.FRI;
            case SUN -> PolicyDay.SAT;
        };
    }
}
