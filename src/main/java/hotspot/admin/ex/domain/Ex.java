package hotspot.admin.ex.domain;

import hotspot.admin.ex.controller.request.CreateExRequest;
import hotspot.admin.ex.controller.request.UpdateExRequest;
import lombok.Builder;
import lombok.Getter;

@Getter
public class Ex {

  private Long exId;

  private String exName;

  private String exDescription;

  @Builder
  public Ex(Long exId, String exName, String exDescription) {
    this.exId = exId;
    this.exName = exName;
    this.exDescription = exDescription;
  }

  public static Ex create(CreateExRequest request) {
    return Ex.builder().exName(request.exName()).exDescription(request.exDescription()).build();
  }

  public void update(UpdateExRequest request) {
    this.exName = request.exName();
    this.exDescription = request.exDescription();
  }
}
