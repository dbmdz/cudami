package de.digitalcollections.cudami.server.business.api.service.identifiable.entity.agent;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.EntityService;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.Set;
import java.util.UUID;

public interface PersonService extends EntityService<Person> {

  PageResponse<Person> findByGeoLocationOfBirth(PageRequest pageRequest, UUID uuid);

  PageResponse<Person> findByGeoLocationOfDeath(PageRequest pageRequest, UUID uuid);

  Set<DigitalObject> getDigitalObjects(UUID uuidPerson);

  default Set<Work> getWorks(Person person) {
    if (person == null) {
      return null;
    }
    return getWorks(person.getUuid());
  }

  Set<Work> getWorks(UUID uuidPerson);
}
