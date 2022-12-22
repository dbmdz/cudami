package de.digitalcollections.cudami.server.business.api.service.identifiable.entity.semantic;

import de.digitalcollections.cudami.server.business.api.service.UniqueObjectService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.semantic.Subject;
import java.util.List;
import java.util.UUID;

public interface SubjectService extends UniqueObjectService<Subject> {

  long count();

  Subject getByUuid(UUID uuid);

  void save(Subject subject) throws ServiceException;

  void update(Subject subject) throws ServiceException;

  boolean delete(List<UUID> uuids);

  PageResponse<Subject> find(PageRequest pageRequest);

  Subject getByTypeAndIdentifier(String type, String namespace, String id) throws ServiceException;
}
