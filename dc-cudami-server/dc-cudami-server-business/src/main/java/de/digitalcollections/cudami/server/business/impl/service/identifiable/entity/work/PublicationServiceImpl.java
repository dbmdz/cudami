package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.work;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work.PublicationRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.PublicationService;
import de.digitalcollections.model.identifiable.entity.work.Publication;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class PublicationServiceImpl implements PublicationService {

  private final PublicationRepository repository;

  public PublicationServiceImpl(PublicationRepository repository) {
    this.repository = repository;
  }

  @Override
  public long count() {
    return repository.count();
  }

  @Override
  public Publication getByUuid(UUID uuid) {
    return repository.getByUuid(uuid);
  }

  @Override
  public Publication save(Publication publication) {
    return repository.save(publication);
  }

  @Override
  public Publication update(Publication publication) {
    return repository.update(publication);
  }

  @Override
  public boolean delete(List<UUID> uuids) {
    return repository.delete(uuids);
  }

  @Override
  public PageResponse<Publication> find(PageRequest pageRequest) {
    return repository.find(pageRequest);
  }
}
