package hotspot.admin.ex.service;

import org.springframework.stereotype.Service;

import hotspot.admin.ex.controller.port.SaveExService;
import hotspot.admin.ex.controller.request.CreateExRequest;
import hotspot.admin.ex.domain.Ex;
import hotspot.admin.ex.service.port.ExRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SaveExServiceImpl implements SaveExService {

  private final ExRepository exRepository;

  @Override
  public void save(CreateExRequest request) {
    Ex ex = Ex.create(request);
    exRepository.save(ex);
  }
}
