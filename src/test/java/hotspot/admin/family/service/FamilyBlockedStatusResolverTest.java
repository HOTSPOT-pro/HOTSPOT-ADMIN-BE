package hotspot.admin.family.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import hotspot.admin.family.domain.FamilyRole;
import hotspot.admin.family.infrastructure.query.dto.FamilyPolicyMemberRow;
import hotspot.admin.family.infrastructure.query.dto.FamilyPolicyTimeOptionRow;
import hotspot.admin.family.infrastructure.query.dto.FamilyPolicyTimePolicyRow;
import hotspot.admin.family.service.port.FamilyPolicyAssignmentRepository;
import hotspot.admin.family.service.port.FamilySubQueryRepository;
import hotspot.admin.policy.domain.PolicyDay;
import hotspot.admin.policy.domain.PolicySnapshot;
import hotspot.admin.policy.domain.PolicyType;

@ExtendWith(MockitoExtension.class)
class FamilyBlockedStatusResolverTest {

    @Mock
    private FamilySubQueryRepository familySubQueryRepository;

    @Mock
    private FamilyPolicyAssignmentRepository familyPolicyAssignmentRepository;

    private FamilyBlockedStatusResolver resolver;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-03-09T02:30:00Z"),
                ZoneId.of("Asia/Seoul")
        );
        resolver = new FamilyBlockedStatusResolver(
                familySubQueryRepository,
                familyPolicyAssignmentRepository,
                new ObjectMapper(),
                fixedClock
        );
    }

    @Test
    void resolveBlockedBySubIdMergesImmediateAndActiveTimePolicy() {
        when(familySubQueryRepository.findFamilyPolicyMembers(9L)).thenReturn(List.of(
                FamilyPolicyMemberRow.builder()
                        .subId(24L)
                        .memberName("A")
                        .familyRole(FamilyRole.CHILD)
                        .blocked(false)
                        .build(),
                FamilyPolicyMemberRow.builder()
                        .subId(25L)
                        .memberName("B")
                        .familyRole(FamilyRole.PARENT)
                        .blocked(true)
                        .build(),
                FamilyPolicyMemberRow.builder()
                        .subId(26L)
                        .memberName("C")
                        .familyRole(FamilyRole.OWNER)
                        .blocked(false)
                        .build()
        ));

        when(familySubQueryRepository.findFamilyTimePolicyOptions(9L)).thenReturn(List.of(
                FamilyPolicyTimeOptionRow.builder()
                        .policyId(101L)
                        .policyType(PolicyType.SCHEDULED)
                        .policySnapshotJson("{\"days\":[\"MON\"],\"startTime\":\"11:00\",\"endTime\":\"12:00\"}")
                        .build(),
                FamilyPolicyTimeOptionRow.builder()
                        .policyId(102L)
                        .policyType(PolicyType.SCHEDULED)
                        .policySnapshotJson("{\"days\":[\"MON\"],\"startTime\":\"01:00\",\"endTime\":\"02:00\"}")
                        .build()
        ));

        when(familySubQueryRepository.findFamilyTimePolicies(9L)).thenReturn(List.of(
                FamilyPolicyTimePolicyRow.builder()
                        .policySubId(1001L)
                        .subId(24L)
                        .policyId(101L)
                        .policyName("active")
                        .modifiedTime(LocalDateTime.of(2026, 3, 9, 10, 30))
                        .build(),
                FamilyPolicyTimePolicyRow.builder()
                        .policySubId(1002L)
                        .subId(26L)
                        .policyId(102L)
                        .policyName("inactive")
                        .modifiedTime(LocalDateTime.of(2026, 3, 9, 0, 30))
                        .build()
        ));

        Map<Long, Boolean> result = resolver.resolveBlockedBySubId(9L);

        assertThat(result).containsEntry(24L, true);
        assertThat(result).containsEntry(25L, true);
        assertThat(result).containsEntry(26L, false);
    }

    @Test
    void resolveBlockedBySubIdTreatsMissingSnapshotAsNotBlocked() {
        when(familySubQueryRepository.findFamilyPolicyMembers(9L)).thenReturn(List.of(
                FamilyPolicyMemberRow.builder()
                        .subId(30L)
                        .memberName("D")
                        .familyRole(FamilyRole.CHILD)
                        .blocked(false)
                        .build()
        ));

        when(familySubQueryRepository.findFamilyTimePolicyOptions(9L)).thenReturn(List.of(
                FamilyPolicyTimeOptionRow.builder()
                        .policyId(201L)
                        .policyType(PolicyType.SCHEDULED)
                        .policySnapshotJson("{\"days\":[\"MON\"],\"startTime\":\"10:00\",\"endTime\":\"12:00\"}")
                        .build()
        ));

        when(familySubQueryRepository.findFamilyTimePolicies(9L)).thenReturn(List.of(
                FamilyPolicyTimePolicyRow.builder()
                        .policySubId(2001L)
                        .subId(30L)
                        .policyId(999L)
                        .policyName("missing")
                        .modifiedTime(LocalDateTime.of(2026, 3, 9, 8, 0))
                        .build()
        ));

        Map<Long, Boolean> result = resolver.resolveBlockedBySubId(9L);

        assertThat(result).containsEntry(30L, false);
    }

    @Test
    void resolveBlockedBySubIdDeactivatesExpiredOncePolicy() {
        when(familySubQueryRepository.findFamilyPolicyMembers(9L)).thenReturn(List.of(
                FamilyPolicyMemberRow.builder()
                        .subId(40L)
                        .memberName("E")
                        .familyRole(FamilyRole.CHILD)
                        .blocked(false)
                        .build()
        ));

        when(familySubQueryRepository.findFamilyTimePolicyOptions(9L)).thenReturn(List.of(
                FamilyPolicyTimeOptionRow.builder()
                        .policyId(301L)
                        .policyType(PolicyType.ONCE)
                        .policySnapshotJson("{\"durationMinutes\":30}")
                        .build()
        ));

        when(familySubQueryRepository.findFamilyTimePolicies(9L)).thenReturn(List.of(
                FamilyPolicyTimePolicyRow.builder()
                        .policySubId(3001L)
                        .subId(40L)
                        .policyId(301L)
                        .policyName("once-expired")
                        .modifiedTime(LocalDateTime.of(2026, 3, 9, 10, 30))
                        .build()
        ));

        Map<Long, Boolean> result = resolver.resolveBlockedBySubId(9L);

        verify(familyPolicyAssignmentRepository).bulkDeactivateTimePoliciesByIds(Set.of(3001L));
        assertThat(result).containsEntry(40L, false);
    }

    @Test
    void scheduledPolicyInRangeThenBlocked() {
        PolicySnapshot snapshot = PolicySnapshot.builder()
                .days(List.of(PolicyDay.MON, PolicyDay.TUE, PolicyDay.WED, PolicyDay.THU, PolicyDay.FRI))
                .startTime("10:00")
                .endTime("12:00")
                .build();

        boolean blocked = FamilyBlockedStatusResolver.isTimePolicyBlockingNow(
                snapshot,
                LocalDateTime.of(2026, 3, 9, 11, 0)
        );

        assertThat(blocked).isTrue();
    }

    @Test
    void scheduledPolicyOutOfRangeThenNotBlocked() {
        PolicySnapshot snapshot = PolicySnapshot.builder()
                .days(List.of(PolicyDay.MON, PolicyDay.TUE, PolicyDay.WED, PolicyDay.THU, PolicyDay.FRI))
                .startTime("10:00")
                .endTime("12:00")
                .build();

        boolean blocked = FamilyBlockedStatusResolver.isTimePolicyBlockingNow(
                snapshot,
                LocalDateTime.of(2026, 3, 9, 9, 59)
        );

        assertThat(blocked).isFalse();
    }

    @Test
    void overnightPolicyUsesPreviousDayWindow() {
        PolicySnapshot snapshot = PolicySnapshot.builder()
                .days(List.of(PolicyDay.MON))
                .startTime("22:00")
                .endTime("07:00")
                .build();

        boolean blockedAfterMidnight = FamilyBlockedStatusResolver.isTimePolicyBlockingNow(
                snapshot,
                LocalDateTime.of(2026, 3, 10, 1, 0)
        );
        boolean blockedNextEvening = FamilyBlockedStatusResolver.isTimePolicyBlockingNow(
                snapshot,
                LocalDateTime.of(2026, 3, 10, 23, 0)
        );

        assertThat(blockedAfterMidnight).isTrue();
        assertThat(blockedNextEvening).isFalse();
    }

    @Test
    void sameStartAndEndMeansAllDayForMatchedDay() {
        PolicySnapshot snapshot = PolicySnapshot.builder()
                .days(List.of(PolicyDay.MON))
                .startTime("00:00")
                .endTime("00:00")
                .build();

        boolean blocked = FamilyBlockedStatusResolver.isTimePolicyBlockingNow(
                snapshot,
                LocalDateTime.of(2026, 3, 9, 23, 59)
        );

        assertThat(blocked).isTrue();
    }
}
