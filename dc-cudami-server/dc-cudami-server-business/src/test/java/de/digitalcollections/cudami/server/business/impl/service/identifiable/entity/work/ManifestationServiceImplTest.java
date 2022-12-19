package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.work;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work.ManifestationRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.relation.EntityRelationService;
import de.digitalcollections.cudami.server.business.impl.service.AbstractServiceImplTest;
import de.digitalcollections.cudami.server.config.HookProperties;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.manifestation.Manifestation;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The ManifestationService")
class ManifestationServiceImplTest extends AbstractServiceImplTest {

  private ManifestationRepository manifestationRepository;
  private EntityRelationService entityRelationService;
  private IdentifierService identifierService;
  private LocaleService localeService;
  private ManifestationServiceImpl manifestationService;
  private UrlAliasService urlAliasService;
  private HookProperties hookProperties;

  @BeforeEach
  public void beforeEach() {
    manifestationRepository = mock(ManifestationRepository.class);
    entityRelationService = mock(EntityRelationService.class);
    identifierService = mock(IdentifierService.class);
    urlAliasService = mock(UrlAliasService.class);
    hookProperties = mock(HookProperties.class);
    localeService = mock(LocaleService.class);
    manifestationService =
        new ManifestationServiceImpl(
            manifestationRepository,
            identifierService,
            urlAliasService,
            hookProperties,
            localeService,
            entityRelationService,
            cudamiConfig);
  }

  @DisplayName(
      "persisting entity relations will fill the objects of the relations with the UUID of the manifestations only")
  @Test
  public void relationObjectsOnlyWithUuid() throws ServiceException {
    UUID uuid = UUID.randomUUID();
    Manifestation manifestation = Manifestation.builder().uuid(uuid).build();
    EntityRelation relation =
        EntityRelation.builder()
            .subject(Person.builder().label("Karl Ranseier").build())
            .predicate("is_least_successful_author_of")
            .object(manifestation)
            .build();
    manifestation.setRelations(List.of(relation));

    manifestationService.persistEntityRelations(manifestation, true);

    Manifestation manifestionWithUUIDOnly = Manifestation.builder().uuid(uuid).build();
    assertThat(
            manifestation.getRelations().stream()
                .map(EntityRelation::getObject)
                .collect(Collectors.toList()))
        .containsExactly(manifestionWithUUIDOnly);
  }
}
