package hotspot.admin.family.controller.port;

import hotspot.admin.family.controller.response.FamilyListResponse;

public interface SearchFamilyByPhoneService {
    FamilyListResponse searchByPhone(String phoneNumber);
}
