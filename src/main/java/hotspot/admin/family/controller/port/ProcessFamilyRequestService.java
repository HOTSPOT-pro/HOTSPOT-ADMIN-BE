package hotspot.admin.family.controller.port;

import hotspot.admin.family.domain.ApplyType;

public interface ProcessFamilyRequestService {
    void approve(ApplyType applyType, Long requestId);

    void reject(ApplyType applyType, Long requestId);
}
