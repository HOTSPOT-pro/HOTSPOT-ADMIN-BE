package hotspot.admin.family.controller.port;

import hotspot.admin.family.controller.response.FamilyPhoneSearchResponse;

public interface SearchFamilyByPhoneService {
    FamilyPhoneSearchResponse searchByPhone(String phoneNumber);
}
