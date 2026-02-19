package hotspot.admin.family.infrastructure.entity;

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
import hotspot.admin.family.domain.Family;
import hotspot.admin.family.domain.PriorityType;
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
@Table(name = "family")
@SQLDelete(sql = "UPDATE family SET is_deleted = true WHERE family_id = ?")
@Where(clause = "is_deleted = false")
public class FamilyEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "family_id")
    private Long familyId;

    @Column(name = "family_num", nullable = false)
    private Integer familyNum;

    @Column(name = "family_data_amount", nullable = false)
    private Long familyDataAmount;

    @Column(name = "priority_type", length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PriorityType priorityType = PriorityType.FIFO;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    public static FamilyEntity domainToEntity(Family family) {
        return FamilyEntity.builder()
                .familyId(family.getFamilyId())
                .familyNum(family.getFamilyNum())
                .familyDataAmount(family.getFamilyDataAmount())
                .priorityType(family.getPriorityType())
                .isDeleted(family.getIsDeleted())
                .build();
    }

    public Family entityToDomain() {
        return Family.builder()
                .familyId(familyId)
                .familyNum(familyNum)
                .familyDataAmount(familyDataAmount)
                .priorityType(priorityType)
                .isDeleted(isDeleted)
                .createdTime(getCreatedTime())
                .modifiedTime(getModifiedTime())
                .build();
    }
}
