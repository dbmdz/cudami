package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.DigitalObjectRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.CudamiServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.CollectionService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.ProjectService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.DigitalObjectLinkedDataFileResourceService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.DigitalObjectRenderingFileResourceService;
import de.digitalcollections.cudami.server.config.HookProperties;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.resource.LinkedDataFileResource;
import de.digitalcollections.model.identifiable.resource.TextFileResource;
import de.digitalcollections.model.text.LocalizedText;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The DigitalObjectService")
class DigitalObjectServiceImplTest {

  private CollectionService collectionService;
  private CudamiConfig cudamiConfig;
  private DigitalObjectServiceImpl service;
  private DigitalObjectRenderingFileResourceService digitalObjectRenderingFileResourceService;
  private DigitalObjectRepository repo;
  protected HookProperties hookProperties;
  private IdentifierRepository identifierRepository;
  private DigitalObjectLinkedDataFileResourceService linkedDataFileResourceService;
  private LocaleService localeService;
  private ProjectService projectService;
  private UrlAliasService urlAliasService;

  @BeforeEach
  public void beforeEach() throws CudamiServiceException {
    repo = mock(DigitalObjectRepository.class);
    collectionService = mock(CollectionService.class);
    digitalObjectRenderingFileResourceService =
        mock(DigitalObjectRenderingFileResourceService.class);
    hookProperties = mock(HookProperties.class);
    identifierRepository = mock(IdentifierRepository.class);
    linkedDataFileResourceService = mock(DigitalObjectLinkedDataFileResourceService.class);
    localeService = mock(LocaleService.class);
    when(localeService.getDefaultLanguage()).thenReturn("de");
    projectService = mock(ProjectService.class);
    urlAliasService = mock(UrlAliasService.class);

    cudamiConfig = mock(CudamiConfig.class);
    CudamiConfig.UrlAlias cudamiConfigUrlAlias = mock(CudamiConfig.UrlAlias.class);
    when(cudamiConfigUrlAlias.getGenerationExcludes()).thenReturn(List.of("DigitalObject"));
    when(cudamiConfig.getUrlAlias()).thenReturn(cudamiConfigUrlAlias);

    service =
        new DigitalObjectServiceImpl(
            repo,
            collectionService,
            projectService,
            identifierRepository,
            urlAliasService,
            linkedDataFileResourceService,
            digitalObjectRenderingFileResourceService,
            hookProperties,
            localeService,
            cudamiConfig);
  }

  @Test
  @DisplayName("can save LinkedDataFileResources for a DigitalObject")
  void saveLinkedDataFileResources() throws ValidationException, IdentifiableServiceException {
    LinkedDataFileResource linkedDataFileResource =
        LinkedDataFileResource.builder()
            .withLabel(Locale.GERMAN, "Linked Data")
            .withContext("https://foo.bar/blubb.xml")
            .withObjectType("XML")
            .withFilename("blubb.xml") // required!!
            .withMimeType(MimeType.MIME_APPLICATION_XML)
            .build();

    Identifier identifier = new Identifier(null, "foo", "bar");

    DigitalObject digitalObject =
        DigitalObject.builder()
            .withLabel(Locale.GERMAN, "deutschsprachiges Label")
            .withLabel(Locale.ENGLISH, "english label")
            .withDescription(Locale.GERMAN, "Beschreibung")
            .withDescription(Locale.ENGLISH, "description")
            .withLinkedDataFileResource(linkedDataFileResource)
            .build();

    DigitalObject savedDigitalObject = digitalObject;
    savedDigitalObject.setUuid(UUID.randomUUID());
    when(repo.save(eq(digitalObject))).thenReturn(savedDigitalObject);
    when(identifierRepository.save(eq(identifier))).thenReturn(identifier);

    DigitalObject persisted = service.save(digitalObject);

    assertThat(persisted.getLinkedDataResources()).hasSize(1);
  }

  @Test
  @DisplayName("fills LinkedDataResources for a retrieved DigitalObject by uuid")
  void fillLinkedDataResourcesForGetByUuidAndLocale() throws IdentifiableServiceException {
    UUID uuid = UUID.randomUUID();

    DigitalObject persistedDigitalObject =
        DigitalObject.builder()
            .withUuid(uuid)
            .withLabel(Locale.GERMAN, "deutschsprachiges Label")
            .withLabel(Locale.ENGLISH, "english label")
            .withDescription(Locale.GERMAN, "Beschreibung")
            .withDescription(Locale.ENGLISH, "description")
            .build();

    new DigitalObject.Builder()
        .withUuid(uuid)
        .withLabel(Locale.GERMAN, "deutschsprachiges Label")
        .withLabel(Locale.ENGLISH, "english label")
        .withDescription(Locale.GERMAN, "Beschreibung")
        .withDescription(Locale.ENGLISH, "description")
        .build();

    // DigitalObject persistedDigitalObject =
    //    new DigitalObjectBuilder()
    //        .withUuid(uuid)
    //        .withLabel(Locale.GERMAN, "deutschsprachiges Label")
    //       .withLabel(Locale.ENGLISH, "english label")
    //       .withDescription(Locale.GERMAN, "Beschreibung")
    //       .withDescription(Locale.ENGLISH, "description")
    //       .build();
    when(repo.getByUuid(eq(uuid))).thenReturn(persistedDigitalObject);

    LinkedDataFileResource persistedLinkedDataFileResource =
        LinkedDataFileResource.builder()
            .withLabel(Locale.GERMAN, "Linked Data")
            .withContext("https://foo.bar/blubb.xml")
            .withObjectType("XML")
            .withFilename("blubb.xml") // required!!
            .withMimeType(MimeType.MIME_APPLICATION_XML)
            .build();
    when(linkedDataFileResourceService.getLinkedDataFileResources(eq(uuid)))
        .thenReturn(List.of(persistedLinkedDataFileResource));

    DigitalObject actual = service.getByUuidAndLocale(uuid, Locale.ROOT);

    assertThat(actual).isNotNull();
    assertThat(actual.getLinkedDataResources()).containsExactly(persistedLinkedDataFileResource);
  }

  @Test
  @DisplayName("can save RenderingResources for a DigitalObject")
  void saveRenderingResources() throws ValidationException, IdentifiableServiceException {
    FileResource renderingResource = new TextFileResource();
    renderingResource.setLabel(new LocalizedText(Locale.GERMAN, "Linked Data"));
    renderingResource.setMimeType(MimeType.fromTypename("text/html"));
    renderingResource.setUri(URI.create("https://bla.bla/foo.html"));
    renderingResource.setUuid(UUID.randomUUID());
    renderingResource.setFilename("foo.html");
    renderingResource.setLabel(new LocalizedText(Locale.GERMAN, "Beschreibung"));

    Identifier identifier = new Identifier(null, "foo", "bar");

    DigitalObject digitalObject =
        DigitalObject.builder()
            .withLabel(Locale.GERMAN, "deutschsprachiges Label")
            .withLabel(Locale.ENGLISH, "english label")
            .withDescription(Locale.GERMAN, "Beschreibung")
            .withDescription(Locale.ENGLISH, "description")
            .withRenderingResource(renderingResource)
            .build();

    DigitalObject savedDigitalObject = digitalObject;
    savedDigitalObject.setUuid(UUID.randomUUID());
    when(repo.save(eq(digitalObject))).thenReturn(savedDigitalObject);
    when(identifierRepository.save(eq(identifier))).thenReturn(identifier);

    DigitalObject persisted = service.save(digitalObject);

    assertThat(persisted.getRenderingResources()).hasSize(1);
    assertThat(persisted.getRenderingResources().get(0)).isEqualTo(renderingResource);
  }

  @Test
  @DisplayName("fills RenderingResources for a retrieved DigitalObject by uuid")
  void fillRenderingResourcesForGetByUuidAndLocale() throws IdentifiableServiceException {
    UUID uuid = UUID.randomUUID();
    DigitalObject persistedDigitalObject =
        DigitalObject.builder()
            .withUuid(uuid)
            .withLabel(Locale.GERMAN, "deutschsprachiges Label")
            .withLabel(Locale.ENGLISH, "english label")
            .withDescription(Locale.GERMAN, "Beschreibung")
            .withDescription(Locale.ENGLISH, "description")
            .build();
    when(repo.getByUuid(eq(uuid))).thenReturn(persistedDigitalObject);

    FileResource persistedRenderingResource = new TextFileResource();
    persistedRenderingResource.setLabel(new LocalizedText(Locale.GERMAN, "Linked Data"));
    persistedRenderingResource.setMimeType(MimeType.fromTypename("text/html"));
    persistedRenderingResource.setUri(URI.create("https://bla.bla/foo.html"));
    persistedRenderingResource.setUuid(UUID.randomUUID());
    persistedRenderingResource.setFilename("foo.html");
    persistedRenderingResource.setLabel(new LocalizedText(Locale.GERMAN, "Beschreibung"));
    when(digitalObjectRenderingFileResourceService.getRenderingFileResources(eq(uuid)))
        .thenReturn(List.of(persistedRenderingResource));

    DigitalObject actual = service.getByUuidAndLocale(uuid, Locale.ROOT);

    assertThat(actual).isNotNull();
    assertThat(actual.getRenderingResources()).containsExactly(persistedRenderingResource);
  }

  /*

  @Test
  @DisplayName("can save and retrieve a DigitalObject with all of its embedded resources")
  void saveDigitalObject() {
    // Insert a license with uuid
    ensureLicense(EXISTING_LICENSE);

    // Insert a corporate body with UUID
    CorporateBody creator =
        new CorporateBodyBuilder()
            .withUuid(UUID.randomUUID())
            .withLabel(Locale.GERMAN, "Körperschaft")
            .withLabel(Locale.ENGLISH, "Corporate Body")
            .build();

    CorporateBodyRepositoryImpl corporateBodyRepository =
        new CorporateBodyRepositoryImpl(jdbi, cudamiConfig);
    corporateBodyRepository.save(creator);

    // Insert a geolocation with UUID
    GeoLocation creationPlace =
        new GeoLocationBuilder()
            .withUuid(UUID.randomUUID())
            .withLabel(Locale.GERMAN, "Ort")
            .build();
    GeoLocationRepositoryImpl geoLocationRepository =
        new GeoLocationRepositoryImpl(jdbi, cudamiConfig);
    geoLocationRepository.save(creationPlace);

    // Insert a LinkedDataFileResource
    LinkedDataFileResource linkedDataFileResource =
        new LinkedDataFileResourceBuilder()
            .withUuid(UUID.randomUUID())
            .withLabel(Locale.GERMAN, "Linked Data")
            .withContext("https://foo.bar/blubb.xml")
            .withObjectType("XML")
            .withFilename("blubb.xml") // required!!
            .withMimeType(MimeType.MIME_APPLICATION_XML)
            .build();

    linkedDataFileResourceRepository.save(linkedDataFileResource);

    // Insert a rendering FileResource
    FileResource renderingResource = new FileResource();
    renderingResource.setUri(URI.create("https://bla.bla/foo.jpg"));
    renderingResource.setMimeType(MimeType.MIME_IMAGE);
    renderingResource.setUuid(UUID.randomUUID());
    renderingResource.setFilename("foo.jpg");
    renderingResource.setLabel(new LocalizedText(Locale.GERMAN, "Zeichnung"));
    FileResourceMetadataRepositoryImpl<FileResource> fileResourceMetadataRepository =
        new FileResourceMetadataRepositoryImpl<FileResource>(jdbi, cudamiConfig);
    fileResourceMetadataRepository.save(renderingResource);

    // Build a CreationInfo object with the formerly persisted contents
    CreationInfo creationInfo =
        new CreationInfoBuilder()
            .withCreator(creator)
            .withDate("2022-02-25")
            .withGeoLocation(creationPlace)
            .build();

    DigitalObject parent =
        repo.save(new DigitalObjectBuilder().withLabel(Locale.GERMAN, "Parent").build());

    DigitalObject digitalObject =
        new DigitalObjectBuilder()
            .withLabel(Locale.GERMAN, "deutschsprachiges Label")
            .withLabel(Locale.ENGLISH, "english label")
            .withDescription(Locale.GERMAN, "Beschreibung")
            .withDescription(Locale.ENGLISH, "description")
            .withLicense(EXISTING_LICENSE)
            .withCreationInfo(creationInfo)
            .withLinkedDataFileResource(linkedDataFileResource)
            .withRenderingResource(renderingResource)
            .withParent(parent)
            .build();

    // The "save" method internally retrieves the object by findOne
    DigitalObject actual = repo.save(digitalObject);

    assertThat(actual.getLabel().getText(Locale.GERMAN)).isEqualTo("deutschsprachiges Label");
    assertThat(actual.getLabel().getText(Locale.ENGLISH)).isEqualTo("english label");
    Paragraph paragraphDe =
        (Paragraph) actual.getDescription().get(Locale.GERMAN).getContentBlocks().get(0);
    assertThat(((Text) paragraphDe.getContentBlocks().get(0)).getText()).isEqualTo("Beschreibung");

    assertThat(actual.getLicense()).isEqualTo(digitalObject.getLicense());

    assertThat(actual.getCreationInfo().getCreator()).isEqualTo(creator);
    assertThat(actual.getCreationInfo().getDate().format(DateTimeFormatter.ISO_DATE))
        .isEqualTo("2022-02-25");
    assertThat(actual.getCreationInfo().getGeoLocation()).isEqualTo(creationPlace);

    assertThat(actual.getLinkedDataResources()).hasSize(1);
    assertThat(actual.getLinkedDataResources().get(0)).isEqualTo(linkedDataFileResource);

    assertThat(actual.getRenderingResources()).hasSize(1);
    assertThat(actual.getRenderingResources().get(0)).isEqualTo(renderingResource);

    assertThat(actual.getParent()).isNotNull();
    assertThat(actual.getParent().getUuid()).isEqualTo(parent.getUuid());
    assertThat(actual.getParent().getLabel()).isEqualTo(parent.getLabel());
  }

   */

}
