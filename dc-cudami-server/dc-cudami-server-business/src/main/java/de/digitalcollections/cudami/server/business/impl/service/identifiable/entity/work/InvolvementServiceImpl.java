package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.work;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work.InvolvementRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.InvolvementService;
import de.digitalcollections.model.identifiable.entity.work.Involvement;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class InvolvementServiceImpl implements InvolvementService {

  private final InvolvementRepository repository;

  public InvolvementServiceImpl(InvolvementRepository repository) {
    this.repository = repository;
  }

  @Override
  public long count() {
    return repository.count();
  }

  @Override
  public Involvement getByUuid(UUID uuid) {
    return repository.getByUuid(uuid);
  }

  @Override
  public Involvement save(Involvement involvement) {
    return repository.save(involvement);
  }

  @Override
  public Involvement update(Involvement involvement) {
    return repository.update(involvement);
  }

  @Override
  public boolean delete(List<UUID> uuids) {
    return repository.delete(uuids);
  }

  @Override
  public PageResponse<Involvement> find(PageRequest pageRequest) {
    return repository.find(pageRequest);
  }
}
