package hotspot.admin.ex.service;

import org.springframework.stereotype.Service;

import hotspot.admin.ex.controller.port.UpdateExService;
import hotspot.admin.ex.controller.request.UpdateExRequest;
import hotspot.admin.ex.domain.Ex;
import hotspot.admin.ex.service.port.ExRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateExServiceImpl implements UpdateExService {

  private final ExRepository exRepository;

  @Override
  public void update(long exId, UpdateExRequest request) {
    Ex ex = exRepository.findById(exId);

    ex.update(request);

    exRepository.update(ex);
  }
}
