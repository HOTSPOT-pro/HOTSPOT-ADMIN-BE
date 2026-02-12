package hotspot.admin.ex.service.port;

import hotspot.admin.ex.domain.Ex;

public interface ExRepository {

  Ex findById(long exId);

  void save(Ex ex);

  void update(Ex updatedEx);
}
