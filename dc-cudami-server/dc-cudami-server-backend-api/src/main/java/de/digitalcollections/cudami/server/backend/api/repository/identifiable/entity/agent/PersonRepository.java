package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.entity.agent.Person;
import de.digitalcollections.model.api.identifiable.entity.work.Work;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.Set;
import java.util.UUID;

public interface PersonRepository<P extends Person> extends EntityRepository<P> {

  PageResponse<P> findByLocationOfBirth(PageRequest pageRequest, UUID uuidGeoLocation);

  PageResponse<P> findByLocationOfDeath(PageRequest pageRequest, UUID uuidGeoLocation);

  Set<DigitalObject> getDigitalObjects(UUID uuidPerson);

  default Set<Work> getWorks(P person) {
    if (person == null) {
      return null;
    }
    return getWorks(person.getUuid());
  }

  Set<Work> getWorks(UUID uuidPerson);
}
