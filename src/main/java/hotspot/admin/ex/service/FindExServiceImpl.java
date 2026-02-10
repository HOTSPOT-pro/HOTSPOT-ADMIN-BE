package hotspot.admin.ex.service;

import org.springframework.stereotype.Service;

import hotspot.admin.ex.controller.port.FindExService;
import hotspot.admin.ex.controller.response.ExResponse;
import hotspot.admin.ex.domain.Ex;
import hotspot.admin.ex.domain.mapper.ExResponseMapper;
import hotspot.admin.ex.service.port.ExRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindExServiceImpl implements FindExService {

  private final ExRepository exRepository;

  @Override
  public ExResponse findEx(Long exId) {

    Ex ex = exRepository.findById(exId);
    return ExResponseMapper.toExResponse(ex);
  }
}
