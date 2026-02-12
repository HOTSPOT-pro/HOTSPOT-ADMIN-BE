package hotspot.admin.ex.infrastructure.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import hotspot.admin.ex.domain.Ex;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "EX")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long exId;

  private String exName;

  private String exDescription;

  public static ExEntity domainToEntity(Ex ex) {
    return ExEntity.builder()
        .exId(ex.getExId())
        .exName(ex.getExName())
        .exDescription(ex.getExDescription())
        .build();
  }

  public Ex entityToDomain() {
    return Ex.builder().exId(exId).exName(exName).exDescription(exDescription).build();
  }
}
