package hotspot.admin.ex.controller.port;

import hotspot.admin.ex.controller.request.UpdateExRequest;

public interface UpdateExService {

  void update(long exId, UpdateExRequest request);
}
