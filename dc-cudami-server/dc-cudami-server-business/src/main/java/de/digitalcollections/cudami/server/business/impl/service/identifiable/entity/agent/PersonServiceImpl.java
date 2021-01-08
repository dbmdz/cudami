package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.agent;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent.PersonRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.agent.PersonService;
import de.digitalcollections.cudami.server.business.impl.service.identifiable.IdentifiableServiceImpl;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.entity.agent.Person;
import de.digitalcollections.model.api.identifiable.entity.work.Work;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PersonServiceImpl extends IdentifiableServiceImpl<Person> implements PersonService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PersonServiceImpl.class);

  @Autowired
  public PersonServiceImpl(PersonRepository repository) {
    super(repository);
  }

  @Override
  public PageResponse<Person> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial) {
    PageResponse<Person> result =
        ((PersonRepository) repository).findByLanguageAndInitial(pageRequest, language, initial);
    return result;
  }

  @Override
  public PageResponse<Person> findByLocationOfBirth(PageRequest pageRequest, UUID uuidGeoLocation) {
    PageResponse<Person> result =
        ((PersonRepository) repository).findByLocationOfBirth(pageRequest, uuidGeoLocation);
    return result;
  }

  @Override
  public PageResponse<Person> findByLocationOfDeath(PageRequest pageRequest, UUID uuidGeoLocation) {
    PageResponse<Person> result =
        ((PersonRepository) repository).findByLocationOfDeath(pageRequest, uuidGeoLocation);
    return result;
  }

  @Override
  public Set<Work> getWorks(Person person) {
    return PersonService.super.getWorks(person);
  }

  @Override
  public Set<Work> getWorks(UUID uuidPerson) {
    return ((PersonRepository) repository).getWorks(uuidPerson);
  }

  @Override
  public Set<DigitalObject> getDigitalObjects(UUID uuidPerson) {
    return ((PersonRepository) repository).getDigitalObjects(uuidPerson);
  }
}
