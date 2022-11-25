package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.work;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.model.config.CudamiConfig.UrlAlias;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work.ManifestationRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.PublisherService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.CudamiServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.relation.EntityRelationService;
import de.digitalcollections.cudami.server.config.HookProperties;
import de.digitalcollections.model.RelationSpecification;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.geo.location.HumanSettlement;
import de.digitalcollections.model.identifiable.entity.work.Manifestation;
import de.digitalcollections.model.identifiable.entity.work.Publisher;
import de.digitalcollections.model.text.LocalizedText;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The ManifestationService")
class ManifestationServiceImplTest {

  private ManifestationRepository manifestationRepository;
  private EntityRelationService entityRelationService;
  private IdentifierService identifierService;
  private LocaleService localeService;
  private ManifestationServiceImpl manifestationService;
  private PublisherService publisherService;
  private UrlAliasService urlAliasService;
  private CudamiConfig cudamiConfig;
  private HookProperties hookProperties;

  @BeforeEach
  public void beforeEach() {
    manifestationRepository = mock(ManifestationRepository.class);
    entityRelationService = mock(EntityRelationService.class);
    identifierService = mock(IdentifierService.class);
    publisherService = mock(PublisherService.class);
    urlAliasService = mock(UrlAliasService.class);
    hookProperties = mock(HookProperties.class);
    localeService = mock(LocaleService.class);
    cudamiConfig = mock(CudamiConfig.class);
    UrlAlias urlAliasConfig = mock(UrlAlias.class);
    when(cudamiConfig.getUrlAlias()).thenReturn(urlAliasConfig);
    when(urlAliasConfig.getGenerationExcludes()).thenReturn(List.of("MANIFESTATION"));
    manifestationService =
        new ManifestationServiceImpl(
            manifestationRepository,
            identifierService,
            publisherService,
            urlAliasService,
            hookProperties,
            localeService,
            entityRelationService,
            cudamiConfig);
  }

  @DisplayName("fills no publishers on save when no publishers exist")
  @Test
  public void noPublishersOnSave()
      throws ValidationException, IdentifiableServiceException, CudamiServiceException {
    Manifestation manifestation = Manifestation.builder().label("foo").build();

    when(manifestationRepository.save(any(Manifestation.class)))
        .thenAnswer(i -> i.getArguments()[0]);
    manifestationService.save(manifestation);

    verify(publisherService, never()).getByUuid(any(UUID.class));
  }

  @DisplayName("fills no publishers on update when no publishers exist")
  @Test
  public void noPublishersOnUpdate()
      throws ValidationException, IdentifiableServiceException, CudamiServiceException {
    Manifestation manifestation = Manifestation.builder().label("foo").randomUuid().build();

    when(manifestationRepository.getByUuid(any(UUID.class))).thenReturn(manifestation);
    when(manifestationRepository.update(any(Manifestation.class)))
        .thenAnswer(i -> i.getArguments()[0]);
    manifestationService.update(manifestation);

    verify(publisherService, never()).getByUuid(any(UUID.class));
  }

  @DisplayName("can return a saved manifestation with filled publishers")
  @Test
  public void filledPublishersOnSave()
      throws CudamiServiceException, ValidationException, IdentifiableServiceException {
    UUID publisherUuid = UUID.randomUUID();
    Publisher publisher =
        Publisher.builder()
            .agent(
                Person.builder()
                    .label("Karl Ranseier")
                    .name(new LocalizedText(Locale.GERMAN, "Karl Ranseier"))
                    .build())
            .location(
                HumanSettlement.builder()
                    .label("Köln")
                    .name(new LocalizedText(Locale.GERMAN, "Köln"))
                    .build())
            .uuid(publisherUuid)
            .build();
    Manifestation manifestation =
        Manifestation.builder().label("foo").publishers(List.of(publisher)).build();
    Manifestation savedManifestation =
        Manifestation.builder()
            .uuid(UUID.randomUUID())
            .label("foo")
            .publishers(List.of(Publisher.builder().uuid(publisherUuid).build()))
            .build();

    when(manifestationRepository.save(any(Manifestation.class))).thenReturn(savedManifestation);
    when(publisherService.getByUuid(eq(publisherUuid))).thenReturn(publisher);

    Manifestation actualManifestation = manifestationService.save(manifestation);
    assertThat(actualManifestation.getPublishers()).containsExactly(publisher);
  }

  @DisplayName("can return an updated manifestation with filled publishers")
  @Test
  public void filledPublishersOnUpdate()
      throws CudamiServiceException, ValidationException, IdentifiableServiceException {
    UUID publisherUuid1 = UUID.randomUUID();
    Publisher publisher1 =
        Publisher.builder()
            .agent(
                Person.builder()
                    .label("Karl Ranseier")
                    .name(new LocalizedText(Locale.GERMAN, "Karl Ranseier"))
                    .build())
            .location(
                HumanSettlement.builder()
                    .label("Köln")
                    .name(new LocalizedText(Locale.GERMAN, "Köln"))
                    .build())
            .uuid(publisherUuid1)
            .build();
    UUID publisherUuid2 = UUID.randomUUID();
    Publisher publisher2 =
        Publisher.builder()
            .agent(
                Person.builder()
                    .label("Hans Dampf")
                    .name(new LocalizedText(Locale.GERMAN, "Hans Dampf"))
                    .build())
            .location(
                HumanSettlement.builder()
                    .label("in allen Gassen")
                    .name(new LocalizedText(Locale.GERMAN, "in allen Gassen"))
                    .build())
            .uuid(publisherUuid2)
            .build();
    UUID manifestationUuid = UUID.randomUUID();
    Manifestation manifestation =
        Manifestation.builder()
            .label("foo")
            .publishers(List.of(publisher1, publisher2))
            .identifier("foo", "bar")
            .uuid(manifestationUuid)
            .build();
    Manifestation updatedManifestation =
        Manifestation.builder()
            .uuid(manifestationUuid)
            .label("foo")
            .publishers(
                List.of(
                    Publisher.builder().uuid(publisherUuid1).build(),
                    Publisher.builder().uuid(publisherUuid2).build()))
            .build();

    when(manifestationRepository.getByUuid(eq(manifestationUuid))).thenReturn(manifestation);
    when(manifestationRepository.update(any(Manifestation.class))).thenReturn(updatedManifestation);
    when(publisherService.getByUuid(eq(publisherUuid1))).thenReturn(publisher1);
    when(publisherService.getByUuid(eq(publisherUuid2))).thenReturn(publisher2);

    Manifestation actualManifestation = manifestationService.update(manifestation);
    assertThat(actualManifestation.getPublishers()).containsExactly(publisher1, publisher2);
  }

  @DisplayName("can return a saved manifestation with filled parents")
  @Test
  public void filledParentsOnSave()
      throws CudamiServiceException, ValidationException, IdentifiableServiceException {
    UUID parentManifestationUuid = UUID.randomUUID();
    Manifestation parentManifestation =
        Manifestation.builder().uuid(parentManifestationUuid).label("parent").build();
    RelationSpecification<Manifestation> parent =
        RelationSpecification.<Manifestation>builder()
            .title("Titel")
            .sortKey("SortKey")
            .subject(parentManifestation)
            .build();
    Manifestation manifestation = Manifestation.builder().label("foo").parent(parent).build();

    Manifestation savedManifestation =
        Manifestation.builder()
            .uuid(UUID.randomUUID())
            .label("foo")
            .parents(List.of(parent))
            .build();

    when(manifestationRepository.save(any(Manifestation.class))).thenReturn(savedManifestation);
    when(manifestationRepository.getByUuid(eq(parentManifestationUuid)))
        .thenReturn(parentManifestation);

    Manifestation actualManifestation = manifestationService.save(manifestation);
    assertThat(actualManifestation.getParents()).containsExactly(parent);
  }

  @DisplayName("can return an updated manifestation with filled parents")
  @Test
  public void fillParentsOnUpdate() throws ValidationException, IdentifiableServiceException {
    UUID parentManifestationUuid = UUID.randomUUID();
    Manifestation parentManifestation =
        Manifestation.builder().uuid(parentManifestationUuid).label("parent").build();
    RelationSpecification<Manifestation> parent =
        RelationSpecification.<Manifestation>builder()
            .title("Titel")
            .sortKey("SortKey")
            .subject(parentManifestation)
            .build();
    UUID manifestationUuid = UUID.randomUUID();
    Manifestation manifestation =
        Manifestation.builder().uuid(manifestationUuid).label("foo").parent(parent).build();
    RelationSpecification<Manifestation> rawParent =
        RelationSpecification.<Manifestation>builder()
            .title("Titel")
            .sortKey("SortKey")
            .subject(Manifestation.builder().uuid(parentManifestationUuid).build())
            .build();

    Manifestation updateManifestation =
        Manifestation.builder().uuid(manifestationUuid).label("foo").parent(rawParent).build();

    when(manifestationRepository.getByUuid(eq(parentManifestationUuid)))
        .thenReturn(parentManifestation);
    when(manifestationRepository.getByUuid(eq(manifestationUuid))).thenReturn(manifestation);
    when(manifestationRepository.update(any(Manifestation.class))).thenReturn(updateManifestation);

    Manifestation actualManifestation = manifestationService.update(manifestation);
    assertThat(actualManifestation.getParents()).containsExactly(parent);
  }

  @DisplayName("fills the parent manifestations on retrieval by uuid")
  @Test
  public void fillParentsOnRetrievalByUuid() throws IdentifiableServiceException {
    UUID manifestationUuid = UUID.randomUUID();
    UUID parentManifestationUuid = UUID.randomUUID();
    RelationSpecification<Manifestation> rawParent =
        RelationSpecification.<Manifestation>builder()
            .title("Titel")
            .sortKey("SortKey")
            .subject(Manifestation.builder().uuid(parentManifestationUuid).build())
            .build();
    Manifestation persistedManifestation =
        Manifestation.builder().uuid(manifestationUuid).label("foo").parent(rawParent).build();

    when(manifestationRepository.getByUuid(eq(manifestationUuid)))
        .thenReturn(persistedManifestation);
    when(manifestationRepository.getByUuid(eq(parentManifestationUuid)))
        .thenReturn(new Manifestation());

    Manifestation actualManifestation = manifestationService.getByUuid(manifestationUuid);
    assertThat(actualManifestation.getParents()).hasSize(1);
  }
}
