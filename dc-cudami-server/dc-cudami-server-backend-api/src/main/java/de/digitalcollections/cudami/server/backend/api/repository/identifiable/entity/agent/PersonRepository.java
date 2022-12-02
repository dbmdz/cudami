package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent;

import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.entity.work.Work;
import java.util.Set;
import java.util.UUID;

public interface PersonRepository extends AgentRepository<Person> {

  @Override
  Set<DigitalObject> getDigitalObjects(UUID uuidPerson);

  @Override
  default Set<Work> getWorks(Person person) {
    if (person == null) {
      return null;
    }
    return getWorks(person.getUuid());
  }

  @Override
  Set<Work> getWorks(UUID uuidPerson);
}
