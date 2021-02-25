package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.work.Work;
import java.util.Set;
import java.util.UUID;

public interface PersonRepository extends EntityRepository<Person> {

  Set<DigitalObject> getDigitalObjects(UUID uuidPerson);

  default Set<Work> getWorks(Person person) {
    if (person == null) {
      return null;
    }
    return getWorks(person.getUuid());
  }

  Set<Work> getWorks(UUID uuidPerson);
}
