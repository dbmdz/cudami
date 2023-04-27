package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.item.Item;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.Set;
import java.util.UUID;

/** Repository for Work persistence handling. */
public interface WorkRepository extends EntityRepository<Work> {

  PageResponse<Work> findByPerson(UUID personUuid) throws RepositoryException;

  default PageResponse<Work> findEmbeddedWorks(Work work, PageRequest pageRequest)
      throws RepositoryException {
    if (work == null) {
      throw new IllegalArgumentException("find failed: given object must not be null");
    }
    return findEmbeddedWorks(work.getUuid(), pageRequest);
  }

  PageResponse<Work> findEmbeddedWorks(UUID uuid, PageRequest pageRequest)
      throws RepositoryException;

  default Work getByItem(Item item) throws RepositoryException {
    if (item == null) {
      throw new IllegalArgumentException("get failed: given object must not be null");
    }
    return getByItem(item.getUuid());
  }

  Work getByItem(UUID itemUuid) throws RepositoryException;

  default Set<Work> getByPerson(Person person) throws RepositoryException {
    if (person == null) {
      throw new IllegalArgumentException("get failed: given object must not be null");
    }
    return getByPerson(person.getUuid());
  }

  Set<Work> getByPerson(UUID personUuid) throws RepositoryException;
}
