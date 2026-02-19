package hotspot.admin.plan.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import hotspot.admin.common.BaseEntity;
import hotspot.admin.plan.domain.DataPeriod;
import hotspot.admin.plan.domain.Plan;
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
@Table(name = "plan")
@SQLDelete(sql = "UPDATE plan SET is_deleted = true WHERE plan_id = ?")
@Where(clause = "is_deleted = false")
public class PlanEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_id")
    private Long planId;

    @Column(name = "plan_name", length = 20, nullable = false)
    private String planName;

    @Column(name = "plan_data_amount", nullable = false)
    private Long planDataAmount;

    @Column(name = "data_period", length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    private DataPeriod dataPeriod;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    public static PlanEntity domainToEntity(Plan plan) {
        return PlanEntity.builder()
                .planId(plan.getPlanId())
                .planName(plan.getPlanName())
                .planDataAmount(plan.getPlanDataAmount())
                .dataPeriod(plan.getDataPeriod())
                .isDeleted(plan.getIsDeleted())
                .build();
    }

    public Plan entityToDomain() {
        return Plan.builder()
                .planId(planId)
                .planName(planName)
                .planDataAmount(planDataAmount)
                .dataPeriod(dataPeriod)
                .isDeleted(isDeleted)
                .createdTime(getCreatedTime())
                .modifiedTime(getModifiedTime())
                .build();
    }
}
