package hotspot.admin.auth.infrastructure.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import hotspot.admin.auth.domain.Admin;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Table(name = "admin")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AdminEntity {

    @Id
    @Column(name = "admin_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adminId;

    @Column(name = "admin_code", nullable = false, unique = true)
    private String adminCode;

    public Admin entityToDomain() { return Admin.builder().adminId(adminId).adminCode(adminCode).build(); }

}
