package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.DigitalObjectRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.DigitalObjectLinkedDataFileResourceRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.CollectionService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.ProjectService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.ItemService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.DigitalObjectLinkedDataFileResourceService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.DigitalObjectRenderingFileResourceService;
import de.digitalcollections.cudami.server.business.impl.service.AbstractServiceImplTest;
import de.digitalcollections.cudami.server.config.HookProperties;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.entity.item.Item;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.resource.FileResourceType;
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
class DigitalObjectServiceImplTest extends AbstractServiceImplTest {

  private CollectionService collectionService;
  private DigitalObjectServiceImpl service;
  private DigitalObjectLinkedDataFileResourceService digitalObjectLinkedDataFileResourceService;
  private DigitalObjectRenderingFileResourceService digitalObjectRenderingFileResourceService;
  private DigitalObjectRepository repo;
  protected HookProperties hookProperties;
  private IdentifierService identifierService;
  private ItemService itemService;
  private LocaleService localeService;
  private ProjectService projectService;
  private UrlAliasService urlAliasService;
  private DigitalObjectLinkedDataFileResourceRepository
      digitalObjectLinkedDataFileResourceRepository;

  @Override
  @BeforeEach
  public void beforeEach() throws Exception {
    super.beforeEach();
    repo = mock(DigitalObjectRepository.class);
    collectionService = mock(CollectionService.class);
    digitalObjectLinkedDataFileResourceService =
        mock(DigitalObjectLinkedDataFileResourceService.class);
    digitalObjectRenderingFileResourceService =
        mock(DigitalObjectRenderingFileResourceService.class);
    hookProperties = mock(HookProperties.class);
    identifierService = mock(IdentifierService.class);
    itemService = mock(ItemService.class);
    localeService = mock(LocaleService.class);
    when(localeService.getDefaultLanguage()).thenReturn("de");
    projectService = mock(ProjectService.class);
    urlAliasService = mock(UrlAliasService.class);
    digitalObjectLinkedDataFileResourceRepository =
        mock(DigitalObjectLinkedDataFileResourceRepository.class);

    service =
        new DigitalObjectServiceImpl(
            repo,
            collectionService,
            projectService,
            identifierService,
            itemService,
            urlAliasService,
            digitalObjectLinkedDataFileResourceService,
            digitalObjectRenderingFileResourceService,
            hookProperties,
            localeService,
            cudamiConfig);
  }

  @Test
  @DisplayName("can save LinkedDataFileResources for a DigitalObject")
  void saveLinkedDataFileResources() throws ValidationException, ServiceException {
    LinkedDataFileResource linkedDataFileResource =
        LinkedDataFileResource.builder()
            .label(Locale.GERMAN, "Linked Data")
            .context("https://foo.bar/blubb.xml")
            .objectType("XML")
            .filename("blubb.xml") // required!!
            .mimeType(MimeType.MIME_APPLICATION_XML)
            .build();

    Identifier identifier = new Identifier(null, "foo", "bar");

    DigitalObject digitalObject =
        DigitalObject.builder()
            .label(Locale.GERMAN, "deutschsprachiges Label")
            .label(Locale.ENGLISH, "english label")
            .description(Locale.GERMAN, "Beschreibung")
            .description(Locale.ENGLISH, "description")
            .linkedDataFileResource(linkedDataFileResource)
            .build();

    DigitalObject savedDigitalObject = createDeepCopy(digitalObject);
    savedDigitalObject.setUuid(UUID.randomUUID());

    service.save(digitalObject);

    assertThat(digitalObject.getLinkedDataResources()).hasSize(1);
  }

  @Test
  @DisplayName("fills LinkedDataResources for a retrieved DigitalObject by uuid")
  void fillLinkedDataResourcesForGetByUuidAndLocale() throws ServiceException {
    UUID uuid = UUID.randomUUID();

    DigitalObject persistedDigitalObject =
        DigitalObject.builder()
            .uuid(uuid)
            .label(Locale.GERMAN, "deutschsprachiges Label")
            .label(Locale.ENGLISH, "english label")
            .description(Locale.GERMAN, "Beschreibung")
            .description(Locale.ENGLISH, "description")
            .build();

    DigitalObject.builder()
        .uuid(uuid)
        .label(Locale.GERMAN, "deutschsprachiges Label")
        .label(Locale.ENGLISH, "english label")
        .description(Locale.GERMAN, "Beschreibung")
        .description(Locale.ENGLISH, "description")
        .build();

    when(repo.getByUuid(eq(uuid))).thenReturn(persistedDigitalObject);

    LinkedDataFileResource persistedLinkedDataFileResource =
        LinkedDataFileResource.builder()
            .label(Locale.GERMAN, "Linked Data")
            .context("https://foo.bar/blubb.xml")
            .objectType("XML")
            .filename("blubb.xml") // required!!
            .mimeType(MimeType.MIME_APPLICATION_XML)
            .build();
    when(digitalObjectLinkedDataFileResourceService.getLinkedDataFileResources(eq(uuid)))
        .thenReturn(List.of(persistedLinkedDataFileResource));

    DigitalObject actual = service.getByUuidAndLocale(uuid, Locale.ROOT);

    assertThat(actual).isNotNull();
    assertThat(actual.getLinkedDataResources()).containsExactly(persistedLinkedDataFileResource);
  }

  @Test
  @DisplayName("can save RenderingResources for a DigitalObject")
  void saveRenderingResources() throws ValidationException, ServiceException {
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
            .label(Locale.GERMAN, "deutschsprachiges Label")
            .label(Locale.ENGLISH, "english label")
            .description(Locale.GERMAN, "Beschreibung")
            .description(Locale.ENGLISH, "description")
            .renderingResource(renderingResource)
            .build();

    service.save(digitalObject);

    assertThat(digitalObject.getRenderingResources()).hasSize(1);
    assertThat(digitalObject.getRenderingResources().get(0)).isEqualTo(renderingResource);
  }

  @Test
  @DisplayName("fills RenderingResources for a retrieved DigitalObject by uuid and locale")
  void fillRenderingResourcesForGetByUuidAndLocale() throws ServiceException {
    UUID uuid = UUID.randomUUID();
    DigitalObject persistedDigitalObject =
        DigitalObject.builder()
            .uuid(uuid)
            .label(Locale.GERMAN, "deutschsprachiges Label")
            .label(Locale.ENGLISH, "english label")
            .description(Locale.GERMAN, "Beschreibung")
            .description(Locale.ENGLISH, "description")
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

  @Test
  @DisplayName(
      "fills RenderingResources for a retrieved DigitalObject by identifier (id and namespace)")
  void fillRenderingResourceForGetByIdentfier() throws ServiceException {
    UUID uuid = UUID.randomUUID();
    DigitalObject persistedDigitalObject =
        DigitalObject.builder()
            .uuid(uuid)
            .label(Locale.GERMAN, "deutschsprachiges Label")
            .label(Locale.ENGLISH, "english label")
            .description(Locale.GERMAN, "Beschreibung")
            .description(Locale.ENGLISH, "description")
            .build();
    when(repo.getByIdentifier(any(Identifier.class))).thenReturn(persistedDigitalObject);

    FileResource persistedRenderingResource = new TextFileResource();
    persistedRenderingResource.setLabel(new LocalizedText(Locale.GERMAN, "Linked Data"));
    persistedRenderingResource.setMimeType(MimeType.fromTypename("text/html"));
    persistedRenderingResource.setUri(URI.create("https://bla.bla/foo.html"));
    persistedRenderingResource.setUuid(UUID.randomUUID());
    persistedRenderingResource.setFilename("foo.html");
    persistedRenderingResource.setLabel(new LocalizedText(Locale.GERMAN, "Beschreibung"));
    when(digitalObjectRenderingFileResourceService.getRenderingFileResources(eq(uuid)))
        .thenReturn(List.of(persistedRenderingResource));

    DigitalObject actual = service.getByIdentifier("foo", "bar");
    assertThat(actual).isNotNull();
    assertThat(actual.getRenderingResources()).containsExactly(persistedRenderingResource);

    actual = service.getByIdentifier(new Identifier(null, "foo", "bar"));
    assertThat(actual).isNotNull();
    assertThat(actual.getRenderingResources()).containsExactly(persistedRenderingResource);
  }

  @Test
  @DisplayName("fills RenderingResources for a retrieved DigitalObject by uuid")
  void fillRenderingResourceForGetByUUID() throws ServiceException {
    UUID uuid = UUID.randomUUID();
    DigitalObject persistedDigitalObject =
        DigitalObject.builder()
            .uuid(uuid)
            .label(Locale.GERMAN, "deutschsprachiges Label")
            .label(Locale.ENGLISH, "english label")
            .description(Locale.GERMAN, "Beschreibung")
            .description(Locale.ENGLISH, "description")
            .build();
    when(repo.getByUuid(any(UUID.class))).thenReturn(persistedDigitalObject);

    FileResource persistedRenderingResource = new TextFileResource();
    persistedRenderingResource.setLabel(new LocalizedText(Locale.GERMAN, "Linked Data"));
    persistedRenderingResource.setMimeType(MimeType.fromTypename("text/html"));
    persistedRenderingResource.setUri(URI.create("https://bla.bla/foo.html"));
    persistedRenderingResource.setUuid(UUID.randomUUID());
    persistedRenderingResource.setFilename("foo.html");
    persistedRenderingResource.setLabel(new LocalizedText(Locale.GERMAN, "Beschreibung"));
    when(digitalObjectRenderingFileResourceService.getRenderingFileResources(eq(uuid)))
        .thenReturn(List.of(persistedRenderingResource));

    DigitalObject actual = service.getByUuid(uuid);

    assertThat(actual).isNotNull();
    assertThat(actual.getRenderingResources()).containsExactly(persistedRenderingResource);
  }

  @Test
  @DisplayName("fills RenderingResources for a retrieved DigitalObject by refId")
  void fillRenderingResourceForGetByRefId() throws ServiceException {
    UUID uuid = UUID.randomUUID();
    DigitalObject persistedDigitalObject =
        DigitalObject.builder()
            .uuid(uuid)
            .label(Locale.GERMAN, "deutschsprachiges Label")
            .label(Locale.ENGLISH, "english label")
            .description(Locale.GERMAN, "Beschreibung")
            .description(Locale.ENGLISH, "description")
            .refId(42)
            .build();
    when(repo.getByRefId(any(Long.class))).thenReturn(persistedDigitalObject);

    FileResource persistedRenderingResource = new TextFileResource();
    persistedRenderingResource.setLabel(new LocalizedText(Locale.GERMAN, "Linked Data"));
    persistedRenderingResource.setMimeType(MimeType.fromTypename("text/html"));
    persistedRenderingResource.setUri(URI.create("https://bla.bla/foo.html"));
    persistedRenderingResource.setUuid(UUID.randomUUID());
    persistedRenderingResource.setFilename("foo.html");
    persistedRenderingResource.setLabel(new LocalizedText(Locale.GERMAN, "Beschreibung"));
    when(digitalObjectRenderingFileResourceService.getRenderingFileResources(eq(uuid)))
        .thenReturn(List.of(persistedRenderingResource));

    DigitalObject actual = service.getByRefId(42);

    service.getByRefId(42);

    assertThat(actual).isNotNull();
    assertThat(actual.getRenderingResources()).containsExactly(persistedRenderingResource);
  }

  @Test
  @DisplayName(
      "deletes RenderingResources and LinkedDataFileResources of a DigitalObject, when the DigitalObject is delete")
  void deleteRenderingAndLinkedDataFileResources() throws ServiceException, ConflictException {
    UUID uuid = UUID.randomUUID();
    DigitalObject persistedDigitalObject =
        DigitalObject.builder()
            .uuid(uuid)
            .label(Locale.GERMAN, "deutschsprachiges Label")
            .label(Locale.ENGLISH, "english label")
            .description(Locale.GERMAN, "Beschreibung")
            .description(Locale.ENGLISH, "description")
            .renderingResources(
                List.of(FileResource.builder().type(FileResourceType.APPLICATION).build()))
            .linkedDataResources(List.of(LinkedDataFileResource.builder().build()))
            .refId(42)
            .build();
    when(repo.getByUuid(any(UUID.class))).thenReturn(persistedDigitalObject);
    when(repo.delete(any(List.class))).thenReturn(true);

    assertThat(service.delete(uuid)).isTrue();

    verify(repo, times(2)).getByUuid(eq(uuid));
    verify(repo, times(1)).deleteFileResources(eq(uuid));
    verify(repo, times(1)).deleteByUuid(eq(List.of(uuid)));
    verify(digitalObjectLinkedDataFileResourceService, times(1))
        .deleteLinkedDataFileResources(eq(uuid));
    verify(digitalObjectRenderingFileResourceService, times(1))
        .deleteRenderingFileResources(eq(uuid));
  }

  @Test
  @DisplayName("returns false when the given item is null")
  public void addToNullItem() throws ConflictException, ServiceException, ValidationException {
    assertThat(service.addItemToDigitalObject(null, UUID.randomUUID())).isFalse();
  }

  @Test
  @DisplayName("returns false when the given uuid was not found")
  public void addNonexistingDigitalObjectToItem()
      throws ConflictException, ServiceException, ValidationException {
    UUID uuid = UUID.randomUUID();

    when(service.getByUuid(eq(uuid))).thenReturn(null);

    assertThat(service.addItemToDigitalObject(Item.builder().build(), uuid)).isFalse();
  }

  @Test
  @DisplayName("returns true if the digital object is already connected to the item")
  public void addExistingAndAlreadyConntectedDigitalObjectToItem()
      throws ConflictException, ServiceException, ValidationException {
    Item item = Item.builder().uuid(UUID.randomUUID()).build();
    DigitalObject digitalObject =
        DigitalObject.builder().uuid(UUID.randomUUID()).item(item).build();

    when(service.getByUuid(eq(digitalObject.getUuid()))).thenReturn(digitalObject);

    assertThat(service.addItemToDigitalObject(item, digitalObject.getUuid())).isTrue();
  }

  @Test
  @DisplayName(
      "throws ConflictException if the digital object is already connected to another item")
  public void addExistingButOtherwiseConntectedDigitalObjectToItem() throws ServiceException {
    Item item = Item.builder().uuid(UUID.randomUUID()).build();
    Item otherItem = Item.builder().uuid(UUID.randomUUID()).build();
    DigitalObject digitalObject =
        DigitalObject.builder().uuid(UUID.randomUUID()).item(otherItem).build();

    when(service.getByUuid(eq(digitalObject.getUuid()))).thenReturn(digitalObject);
    assertThrows(
        ConflictException.class,
        () -> {
          service.addItemToDigitalObject(item, digitalObject.getUuid());
        });
  }

  @Test
  @DisplayName("returns true when the digital object was successfully connected with the item")
  public void addExistingAndNotConntectedDigitalObjectToItem()
      throws ConflictException, ServiceException, ValidationException {
    Item item = Item.builder().uuid(UUID.randomUUID()).build();
    DigitalObject digitalObject =
        DigitalObject.builder()
            .uuid(UUID.randomUUID())
            .label(LocalizedText.builder().text(Locale.ITALY, "Viva Italia!").build())
            .build();

    when(service.getByUuid(eq(digitalObject.getUuid()))).thenReturn(digitalObject);

    assertThat(service.addItemToDigitalObject(item, digitalObject.getUuid())).isTrue();
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
