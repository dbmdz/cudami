package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.DigitalObjectRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.DigitalObjectLinkedDataFileResourceRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.DigitalObjectRenderingFileResourceRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.CollectionService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.ProjectService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.ItemService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.ManifestationService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.WorkService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.DigitalObjectLinkedDataFileResourceService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.DigitalObjectRenderingFileResourceService;
import de.digitalcollections.cudami.server.business.impl.service.AbstractServiceImplTest;
import de.digitalcollections.cudami.server.config.HookProperties;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.entity.item.Item;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.resource.FileResourceType;
import de.digitalcollections.model.identifiable.resource.LinkedDataFileResource;
import de.digitalcollections.model.identifiable.resource.TextFileResource;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.validation.ValidationException;
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
  private ManifestationService manifestationService;
  private WorkService workService;
  private LocaleService localeService;
  private ProjectService projectService;
  private UrlAliasService urlAliasService;
  private DigitalObjectLinkedDataFileResourceRepository
      digitalObjectLinkedDataFileResourceRepository;
  private DigitalObjectRenderingFileResourceRepository digitalObjectRenderingFileResourceRepository;

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
    manifestationService = mock(ManifestationService.class);
    workService = mock(WorkService.class);
    localeService = mock(LocaleService.class);
    when(localeService.getDefaultLanguage()).thenReturn("de");
    projectService = mock(ProjectService.class);
    urlAliasService = mock(UrlAliasService.class);
    digitalObjectLinkedDataFileResourceRepository =
        mock(DigitalObjectLinkedDataFileResourceRepository.class);
    digitalObjectRenderingFileResourceRepository =
        mock(DigitalObjectRenderingFileResourceRepository.class);

    service =
        new DigitalObjectServiceImpl(
            repo,
            collectionService,
            projectService,
            identifierService,
            itemService,
            manifestationService,
            workService,
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
  @DisplayName("can save RenderingResources for a DigitalObject")
  void saveRenderingResources() throws ValidationException, ServiceException {
    FileResource renderingResource = new TextFileResource();
    renderingResource.setLabel(new LocalizedText(Locale.GERMAN, "Linked Data"));
    renderingResource.setMimeType(MimeType.fromTypename("text/html"));
    renderingResource.setUri(URI.create("https://bla.bla/foo.html"));
    renderingResource.setUuid(UUID.randomUUID());
    renderingResource.setFilename("foo.html");
    renderingResource.setLabel(new LocalizedText(Locale.GERMAN, "Beschreibung"));

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
  @DisplayName(
      "deletes RenderingResources and LinkedDataFileResources of a DigitalObject, when the DigitalObject is delete")
  void deleteRenderingAndLinkedDataFileResources()
      throws ServiceException, ConflictException, RepositoryException {
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
    when(repo.getByExamples(any(List.class))).thenReturn(List.of(persistedDigitalObject));
    when(repo.delete(any(DigitalObject.class))).thenReturn(true);

    assertThat(service.delete(persistedDigitalObject)).isTrue();

    verify(repo, times(1)).getByExamples(eq(List.of(persistedDigitalObject)));
    verify(repo, times(1)).deleteFileResources(eq(persistedDigitalObject));
    verify(repo, times(1)).delete(eq(persistedDigitalObject));
    verify(digitalObjectLinkedDataFileResourceService, times(1))
        .deleteLinkedDataFileResources(eq(persistedDigitalObject));
    verify(digitalObjectRenderingFileResourceService, times(1))
        .deleteRenderingFileResources(eq(persistedDigitalObject));
  }

  @Test
  @DisplayName("returns false when the given item is null")
  public void addToNullItem() throws ConflictException, ServiceException, ValidationException {
    assertThat(service.setItem(createDigitalObject(), null)).isFalse();
  }

  @Test
  @DisplayName("returns false when the given uuid was not found")
  public void addNonexistingDigitalObjectToItem()
      throws ConflictException, ServiceException, ValidationException {
    DigitalObject digitalObject = createDigitalObject();
    when(service.getByExamples(eq(List.of(digitalObject)))).thenReturn(List.of());

    assertThat(service.setItem(digitalObject, Item.builder().build())).isFalse();
  }

  @Test
  @DisplayName("returns true if the digital object is already connected to the item")
  public void addExistingAndAlreadyConntectedDigitalObjectToItem()
      throws ConflictException, ServiceException, ValidationException {
    Item item = Item.builder().uuid(UUID.randomUUID()).build();
    DigitalObject digitalObject =
        DigitalObject.builder().uuid(UUID.randomUUID()).item(item).build();

    when(service.getByExamples(eq(List.of(digitalObject)))).thenReturn(List.of(digitalObject));

    assertThat(service.setItem(digitalObject, item)).isTrue();
  }

  @Test
  @DisplayName(
      "throws ConflictException if the digital object is already connected to another item")
  public void addExistingButOtherwiseConntectedDigitalObjectToItem() throws ServiceException {
    Item item = Item.builder().uuid(UUID.randomUUID()).build();
    Item otherItem = Item.builder().uuid(UUID.randomUUID()).build();
    DigitalObject digitalObject =
        DigitalObject.builder().uuid(UUID.randomUUID()).item(otherItem).build();

    when(service.getByExamples(eq(List.of(digitalObject)))).thenReturn(List.of(digitalObject));
    assertThrows(
        ConflictException.class,
        () -> {
          service.setItem(digitalObject, item);
        });
  }

  @Test
  @DisplayName("returns true when the digital object was successfully connected with the item")
  public void addExistingAndNotConntectedDigitalObjectToItem()
      throws ConflictException, ServiceException, ValidationException, RepositoryException {
    Item item = Item.builder().uuid(UUID.randomUUID()).build();
    DigitalObject digitalObject =
        DigitalObject.builder()
            .uuid(UUID.randomUUID())
            .label(LocalizedText.builder().text(Locale.ITALY, "Viva Italia!").build())
            .build();
    when(repo.getByExamples(eq(List.of(digitalObject)))).thenReturn(List.of(digitalObject));
    when(repo.getByExample(eq(digitalObject))).thenReturn(digitalObject);

    assertThat(service.setItem(digitalObject, item)).isTrue();
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
