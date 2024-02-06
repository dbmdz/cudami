package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.work;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work.ManifestationRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.relation.EntityToEntityRelationService;
import de.digitalcollections.cudami.server.business.impl.service.AbstractServiceImplTest;
import de.digitalcollections.cudami.server.config.HookProperties;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.manifestation.Manifestation;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.validation.ValidationException;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The ManifestationService")
class ManifestationServiceImplTest extends AbstractServiceImplTest {

  private ManifestationRepository manifestationRepository;
  private EntityToEntityRelationService entityRelationService;
  private IdentifierService identifierService;
  private LocaleService localeService;
  private ManifestationServiceImpl manifestationService;
  private UrlAliasService urlAliasService;
  private HookProperties hookProperties;

  @BeforeEach
  public void beforeEach() throws Exception {
    super.beforeEach();
    manifestationRepository = mock(ManifestationRepository.class);
    entityRelationService = mock(EntityToEntityRelationService.class);
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

  @DisplayName("saves the relations to a manifestation, too")
  @Test
  public void saveWithRelations()
      throws ServiceException, RepositoryException, ValidationException {
    UUID authorUuid = UUID.randomUUID();
    Person author =
        Person.builder()
            .label(Locale.GERMAN, "Karl Ranseier")
            .name(new LocalizedText(Locale.GERMAN, "Karl Ranseier"))
            .uuid(authorUuid)
            .build();

    Manifestation manifestationToSave =
        Manifestation.builder()
            .label(Locale.GERMAN, "Erstlingswerk")
            .relation(EntityRelation.builder().subject(author).predicate("is_creator_of").build())
            .build();

    Manifestation savedManifestation =
        Manifestation.builder()
            .uuid(UUID.randomUUID())
            .label(Locale.GERMAN, "Erstlingswerk")
            .relation(EntityRelation.builder().subject(author).predicate("is_creator_of").build())
            .build();

    doAnswer(
            invocation -> {
              Object[] args = invocation.getArguments();
              ((Manifestation) args[0]).setUuid(savedManifestation.getUuid());
              return null;
            })
        .when(manifestationRepository)
        .save(eq(manifestationToSave));

    manifestationService.save(manifestationToSave);

    verify(entityRelationService, times(1))
        .setEntityRelations(
            eq(savedManifestation), eq(manifestationToSave.getRelations()), eq(true));
  }

  @DisplayName("can update a manifestation with relations")
  @Test
  public void updateWithRelations()
      throws RepositoryException, ServiceException, ValidationException {
    UUID authorUuid = UUID.randomUUID();
    Person author =
        Person.builder()
            .label(Locale.GERMAN, "Karl Ranseier")
            .name(new LocalizedText(Locale.GERMAN, "Karl Ranseier"))
            .uuid(authorUuid)
            .build();

    UUID uuid = UUID.randomUUID();
    Manifestation manifestationToUpdate =
        Manifestation.builder()
            .uuid(uuid)
            .label(Locale.GERMAN, "Erstlingswerk")
            .relation(EntityRelation.builder().subject(author).predicate("is_creator_of").build())
            .build();

    doAnswer(
            invocation -> {
              return null;
            })
        .when(manifestationRepository)
        .update(eq(manifestationToUpdate));
    when(manifestationRepository.getByExample(eq(manifestationToUpdate)))
        .thenReturn(manifestationToUpdate);

    manifestationService.update(manifestationToUpdate);

    assertThat(manifestationToUpdate).isEqualTo(manifestationToUpdate);

    verify(entityRelationService, times(1))
        .setEntityRelations(
            eq(manifestationToUpdate), eq(manifestationToUpdate.getRelations()), eq(false));
  }
}
