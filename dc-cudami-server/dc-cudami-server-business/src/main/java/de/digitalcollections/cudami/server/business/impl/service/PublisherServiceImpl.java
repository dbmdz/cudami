package de.digitalcollections.cudami.server.business.impl.service;

import de.digitalcollections.cudami.server.backend.api.repository.PublisherRepository;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.business.api.service.PublisherService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.CudamiServiceException;
import de.digitalcollections.model.identifiable.entity.work.Publisher;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class PublisherServiceImpl implements PublisherService {

  private final PublisherRepository repository;

  public PublisherServiceImpl(PublisherRepository repository) {
    this.repository = repository;
  }

  @Override
  public PageResponse<Publisher> find(PageRequest pageRequest) throws CudamiServiceException {
    try {
      return repository.find(pageRequest);
    } catch (RepositoryException e) {
      throw new CudamiServiceException(
          "Cannot find publishers with pageRequest=" + pageRequest + ": " + e, e);
    }
  }

  @Override
  public Publisher getByUuid(UUID uuid) throws CudamiServiceException {
    try {
      return repository.getByUuid(uuid);
    } catch (RepositoryException e) {
      throw new CudamiServiceException("Cannot retrieve publishers by uuid=" + uuid + ": " + e, e);
    }
  }

  @Override
  public Publisher save(Publisher publisher) throws CudamiServiceException {
    try {
      return repository.save(publisher);
    } catch (RepositoryException e) {
      throw new CudamiServiceException("Cannot save publisher=" + publisher + ": " + e, e);
    }
  }

  @Override
  public Publisher update(Publisher publisher) throws CudamiServiceException {
    try {
      return repository.update(publisher);
    } catch (RepositoryException e) {
      throw new CudamiServiceException("Cannot update publisher=" + publisher + ": " + e, e);
    }
  }

  @Override
  public boolean delete(UUID uuid) throws CudamiServiceException {
    try {
      return repository.deleteByUuid(uuid) == 1;
    } catch (RepositoryException e) {
      throw new CudamiServiceException("Cannot delete publisher by uuid=" + uuid + ": " + e, e);
    }
  }
}
