package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.work;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.model.config.CudamiConfig.UrlAlias;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work.ManifestationRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.CudamiServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.relation.EntityRelationService;
import de.digitalcollections.cudami.server.config.HookProperties;
import de.digitalcollections.model.RelationSpecification;
import de.digitalcollections.model.identifiable.entity.manifestation.Manifestation;
import java.util.List;
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
  private UrlAliasService urlAliasService;
  private CudamiConfig cudamiConfig;
  private HookProperties hookProperties;

  @BeforeEach
  public void beforeEach() {
    manifestationRepository = mock(ManifestationRepository.class);
    entityRelationService = mock(EntityRelationService.class);
    identifierService = mock(IdentifierService.class);
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
            urlAliasService,
            hookProperties,
            localeService,
            entityRelationService,
            cudamiConfig);
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
