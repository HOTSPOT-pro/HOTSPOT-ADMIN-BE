package hotspot.admin.family.infrastructure.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import hotspot.admin.common.BaseEntity;
import hotspot.admin.family.domain.DeleteStatus;
import hotspot.admin.family.domain.FamilyRemoveSchedule;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "family_remove_schedule")
public class FamilyRemoveScheduleEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "family_remove_schedule_id")
    private Long familyRemoveScheduleId;

    @Column(name = "target_sub_id", nullable = false)
    private Long targetSubId;

    @Column(name = "family_id", nullable = false)
    private Long familyId;

    @Column(name = "status", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DeleteStatus status = DeleteStatus.SCHEDULED;

    @Column(name = "schedule_date", nullable = false)
    private LocalDate scheduleDate;

    public static FamilyRemoveScheduleEntity domainToEntity(FamilyRemoveSchedule schedule) {
        return FamilyRemoveScheduleEntity.builder()
                .familyRemoveScheduleId(schedule.getFamilyRemoveScheduleId())
                .targetSubId(schedule.getTargetSubId())
                .familyId(schedule.getFamilyId())
                .status(schedule.getStatus())
                .scheduleDate(schedule.getScheduleDate())
                .build();
    }

    public FamilyRemoveSchedule entityToDomain() {
        return FamilyRemoveSchedule.builder()
                .familyRemoveScheduleId(familyRemoveScheduleId)
                .targetSubId(targetSubId)
                .familyId(familyId)
                .status(status)
                .scheduleDate(scheduleDate)
                .createdTime(getCreatedTime())
                .modifiedTime(getModifiedTime())
                .build();
    }
}
