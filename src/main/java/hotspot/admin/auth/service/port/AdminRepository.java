package hotspot.admin.auth.service.port;

import hotspot.admin.auth.domain.Admin;

public interface AdminRepository {

    Admin findByAdminCode(String adminCode);
}
