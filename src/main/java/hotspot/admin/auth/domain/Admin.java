package hotspot.admin.auth.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Admin {

    private Long adminId;

    private String adminCode;

    @Builder
    public Admin(Long adminId, String adminCode) {
        this.adminId = adminId;
        this.adminCode = adminCode;
    }
}
