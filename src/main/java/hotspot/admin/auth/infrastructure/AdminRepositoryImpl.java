package hotspot.admin.auth.infrastructure;

import org.springframework.stereotype.Repository;

import hotspot.admin.auth.domain.Admin;
import hotspot.admin.auth.infrastructure.entity.AdminEntity;
import hotspot.admin.auth.service.port.AdminRepository;
import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.code.AuthErrorCode;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AdminRepositoryImpl implements AdminRepository {

    private final AdminJpaRepository adminJpaRepository;

    @Override
    public Admin findByAdminCode(String adminCode) {
        return adminJpaRepository
                .findByAdminCode(adminCode)
                .map(AdminEntity::entityToDomain)
                .orElseThrow(() -> new ApplicationException(AuthErrorCode.LOGIN_FAILED));
    }
}
