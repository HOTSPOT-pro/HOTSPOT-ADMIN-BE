package hotspot.admin.appservice.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import hotspot.admin.appservice.domain.AppBlockedService;
import hotspot.admin.common.BaseEntity;
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
@Table(name = "app_blocked_service")
@SQLDelete(sql = "UPDATE app_blocked_service SET is_deleted = true WHERE app_blocked_service_id = ?")
@Where(clause = "is_deleted = false")
public class AppBlockedServiceEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "app_blocked_service_id")
    private Long appBlockedServiceId;

    @Column(name = "blocked_service_name", length = 20, nullable = false)
    private String blockedServiceName;

    @Column(name = "blocked_service_code", length = 30, nullable = false)
    private String blockedServiceCode;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    public static AppBlockedServiceEntity domainToEntity(AppBlockedService appBlockedService) {
        return AppBlockedServiceEntity.builder()
                .appBlockedServiceId(appBlockedService.getAppBlockedServiceId())
                .blockedServiceName(appBlockedService.getBlockedServiceName())
                .blockedServiceCode(appBlockedService.getBlockedServiceCode())
                .isActive(appBlockedService.getIsActive())
                .isDeleted(appBlockedService.getIsDeleted())
                .build();
    }

    public AppBlockedService entityToDomain() {
        return AppBlockedService.builder()
                .appBlockedServiceId(appBlockedServiceId)
                .blockedServiceName(blockedServiceName)
                .blockedServiceCode(blockedServiceCode)
                .isActive(isActive)
                .isDeleted(isDeleted)
                .createdTime(getCreatedTime())
                .modifiedTime(getModifiedTime())
                .build();
    }
}
