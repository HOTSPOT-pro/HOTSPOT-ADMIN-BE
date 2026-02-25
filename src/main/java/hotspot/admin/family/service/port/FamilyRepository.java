package hotspot.admin.family.service.port;

import java.util.List;
import java.util.Optional;

import hotspot.admin.family.controller.response.FamilyListItem;

public interface FamilyRepository {
    List<FamilyListItem> findFamilyList(
            int limit,
            long offset
    );

    long countFamilyList();

    Optional<FamilyListItem> findFamilyByPhoneHash(String phoneHash);
}
