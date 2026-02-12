package hotspot.admin.ex.domain.mapper;

import org.springframework.stereotype.Component;

import hotspot.admin.ex.controller.response.ExResponse;
import hotspot.admin.ex.domain.Ex;

@Component
public class ExResponseMapper {

  public static ExResponse toExResponse(Ex ex) {
    return new ExResponse(ex.getExId(), ex.getExName(), ex.getExDescription());
  }
}
