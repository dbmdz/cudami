package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.entity.agent.Person;
import de.digitalcollections.model.api.identifiable.entity.work.Work;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.Set;
import java.util.UUID;

public interface PersonRepository extends IdentifiableRepository<Person> {

  PageResponse<Person> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial);

  PageResponse<Person> findByLocationOfBirth(PageRequest pageRequest, UUID uuidGeoLocation);

  PageResponse<Person> findByLocationOfDeath(PageRequest pageRequest, UUID uuidGeoLocation);

  default Set<Work> getWorks(Person person) {
    if (person == null) {
      return null;
    }
    return getWorks(person.getUuid());
  }

  Set<Work> getWorks(UUID uuidPerson);

  Set<DigitalObject> getDigitalObjects(UUID uuidPerson);
}
