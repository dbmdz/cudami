package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.semantic;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.semantic.SubjectRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.semantic.SubjectService;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.semantic.Subject;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class SubjectServiceImpl implements SubjectService {

  private final SubjectRepository repository;

  public SubjectServiceImpl(SubjectRepository repository) {
    this.repository = repository;
  }

  @Override
  public long count() {
    return repository.count();
  }

  @Override
  public Subject getByUuid(UUID uuid) {
    return repository.getByUuid(uuid);
  }

  @Override
  public void save(Subject subject) throws ServiceException {
    try {
      repository.save(subject);
    } catch (RepositoryException e) {
      throw new ServiceException("Cannot save subject " + subject.toString(), e);
    }
  }

  @Override
  public void update(Subject subject) throws ServiceException {
    try {
      repository.update(subject);
    } catch (RepositoryException e) {
      throw new ServiceException("Cannot update subject " + subject.toString(), e);
    }
  }

  @Override
  public boolean delete(List<UUID> uuids) {
    return repository.delete(uuids);
  }

  @Override
  public PageResponse<Subject> find(PageRequest pageRequest) {
    return repository.find(pageRequest);
  }

  @Override
  public Subject getByTypeAndIdentifier(String type, String namespace, String id)
      throws ServiceException {
    try {
      return repository.getByTypeAndIdentifier(type, namespace, id);
    } catch (Exception e) {
      throw new ServiceException(
          "cannot get by type=" + type + ", namespace=" + namespace + ", id=" + id + ": " + e, e);
    }
  }
}
