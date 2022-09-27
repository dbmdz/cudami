package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.agent;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent.PersonRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.agent.PersonService;
import de.digitalcollections.cudami.server.config.HookProperties;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

// @Transactional should not be set in derived class to prevent overriding, check base class instead
@Service
public class PersonServiceImpl extends AgentServiceImpl<Person> implements PersonService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PersonServiceImpl.class);

  public PersonServiceImpl(
      PersonRepository repository,
      IdentifierService identifierService,
      UrlAliasService urlAliasService,
      HookProperties hookProperties,
      LocaleService localeService,
      CudamiConfig cudamiConfig) {
    super(
        repository,
        identifierService,
        urlAliasService,
        hookProperties,
        localeService,
        cudamiConfig);
  }

  @Override
  public PageResponse<Person> findByGeoLocationOfBirth(
      PageRequest pageRequest, UUID uuidGeoLocation) {
    Filtering filtering =
        Filtering.builder()
            .add(
                FilterCriterion.builder()
                    .withExpression("placeOfBirth")
                    .isEquals(uuidGeoLocation.toString())
                    .build())
            .build();
    pageRequest.setFiltering(filtering);
    return ((PersonRepository) repository).find(pageRequest);
  }

  @Override
  public PageResponse<Person> findByGeoLocationOfDeath(
      PageRequest pageRequest, UUID uuidGeoLocation) {
    Filtering filtering =
        Filtering.builder()
            .add(
                FilterCriterion.builder()
                    .withExpression("placeOfDeath")
                    .isEquals(uuidGeoLocation.toString())
                    .build())
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
