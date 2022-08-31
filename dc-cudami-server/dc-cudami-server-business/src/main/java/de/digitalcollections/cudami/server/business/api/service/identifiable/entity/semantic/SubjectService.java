package de.digitalcollections.cudami.server.business.api.service.identifiable.entity.semantic;

import de.digitalcollections.model.identifiable.entity.semantic.Subject;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.UUID;

public interface SubjectService {

  long count();

  Subject getByUuid(UUID uuid);

  Subject save(Subject subject);

  Subject update(Subject subject);

  boolean delete(List<UUID> uuids);

  PageResponse<Subject> find(PageRequest pageRequest);
}
