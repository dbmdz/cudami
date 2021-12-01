package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.agent;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent.PersonRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.agent.PersonService;
import de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.EntityServiceImpl;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PersonServiceImpl extends EntityServiceImpl<Person> implements PersonService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PersonServiceImpl.class);

  @Autowired
  public PersonServiceImpl(
      PersonRepository repository,
      IdentifierRepository identifierRepository,
      UrlAliasService urlAliasService) {
    super(repository, identifierRepository, urlAliasService);
  }

  @Override
  public PageResponse<Person> findByLocationOfBirth(PageRequest pageRequest, UUID uuidGeoLocation) {
    Filtering filtering =
        Filtering.defaultBuilder()
            .filter("placeOfBirth")
            .isEquals(uuidGeoLocation.toString())
            .build();
    pageRequest.setFiltering(filtering);
    return ((PersonRepository) repository).find(pageRequest);
  }

  @Override
  public PageResponse<Person> findByLocationOfDeath(PageRequest pageRequest, UUID uuidGeoLocation) {
    Filtering filtering =
        Filtering.defaultBuilder()
            .filter("placeOfDeath")
            .isEquals(uuidGeoLocation.toString())
            .build();
    pageRequest.setFiltering(filtering);
    return ((PersonRepository) repository).find(pageRequest);
  }

  @Override
  public Set<DigitalObject> getDigitalObjects(UUID uuidPerson) {
    return ((PersonRepository) repository).getDigitalObjects(uuidPerson);
  }

  @Override
  public Set<Work> getWorks(UUID uuidPerson) {
    return ((PersonRepository) repository).getWorks(uuidPerson);
  }
}
