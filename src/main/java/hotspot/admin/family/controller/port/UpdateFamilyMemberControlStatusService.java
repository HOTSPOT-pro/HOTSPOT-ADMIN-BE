package hotspot.admin.family.controller.port;

public interface UpdateFamilyMemberControlStatusService {
    void updateMemberControlStatus(Long familyId, Long subId, Long dataLimitGb, Boolean isBlocked);
}
