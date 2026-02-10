package hotspot.admin.ex.infrastructure;

import org.springframework.stereotype.Repository;

import hotspot.admin.common.exception.ApplicationException;
import hotspot.admin.common.exception.code.ExErrorCode;
import hotspot.admin.ex.domain.Ex;
import hotspot.admin.ex.infrastructure.entity.ExEntity;
import hotspot.admin.ex.service.port.ExRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ExRepositoryImpl implements ExRepository {

  private final ExJpaRepository exJpaRepository;

  @Override
  public Ex findById(long exId) {
    return exJpaRepository
        .findById(exId)
        .map(ExEntity::entityToDomain)
        .orElseThrow(() -> new ApplicationException(ExErrorCode.EX_NOT_FOUND));
  }

  @Override
  public void save(Ex ex) {
    exJpaRepository.save(ExEntity.domainToEntity(ex));
  }

  @Override
  public void update(Ex updatedEx) {
    exJpaRepository.save(ExEntity.domainToEntity(updatedEx));
  }
}
