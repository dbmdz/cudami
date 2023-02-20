package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import static de.digitalcollections.cudami.server.backend.impl.asserts.CudamiAssertions.assertThat;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work.ItemRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.AbstractIdentifiableRepositoryImplTest;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifierRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent.CorporateBodyRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent.PersonRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.geo.location.GeoLocationRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource.FileResourceMetadataRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource.LinkedDataFileResourceRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.legal.LicenseRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.semantic.TagRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.model.TestModelFixture;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.IdentifiableObjectType;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.identifiable.entity.digitalobject.CreationInfo;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocation;
import de.digitalcollections.model.identifiable.entity.item.Item;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.resource.LinkedDataFileResource;
import de.digitalcollections.model.legal.License;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.FilterOperation;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.semantic.Tag;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.text.StructuredContent;
import de.digitalcollections.model.text.contentblock.Paragraph;
import de.digitalcollections.model.text.contentblock.Text;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(
    webEnvironment = WebEnvironment.MOCK,
    classes = {DigitalObjectRepositoryImpl.class})
@DisplayName("The DigitalObject Repository")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DigitalObjectRepositoryImplTest
    extends AbstractIdentifiableRepositoryImplTest<DigitalObjectRepositoryImpl> {

  @Autowired private CollectionRepositoryImpl collectionRepositoryImpl;

  @Autowired private CorporateBodyRepositoryImpl corporateBodyRepositoryImpl;

  @Autowired private EntityRepositoryImpl<Agent> agentEntityRepositoryImpl;

  @Autowired
  private FileResourceMetadataRepositoryImpl<FileResource> fileResourceMetadataRepositoryImpl;

  @Autowired private TagRepositoryImpl tagRepository;

  @Autowired private EntityRepositoryImpl<GeoLocation> geoLocationEntityRepositoryImpl;

  @Autowired private GeoLocationRepositoryImpl<GeoLocation> geoLocationRepositoryImpl;

  @Autowired private IdentifierRepositoryImpl identifierRepositoryImpl;

  @Autowired private LicenseRepositoryImpl licenseRepository;

  @Autowired private LinkedDataFileResourceRepositoryImpl linkedDataFileResourceRepository;

  @Autowired private PersonRepositoryImpl personRepositoryImpl;
  @Autowired private ItemRepository itemRepository;

  private static final License EXISTING_LICENSE =
      License.builder()
          .uuid(UUID.randomUUID())
          .acronym("CC0 1.0")
          .url("http://rightsstatements.org/vocab/NoC-NC/1.0/")
          .label(Locale.GERMAN, "Kein Urheberrechtsschutz – nur nicht-kommerzielle Nutzung erlaubt")
          .label(Locale.ENGLISH, "No Copyright – Non-Commercial Use Only")
          .build();

  @BeforeEach
  public void beforeEach() {
    repo = new DigitalObjectRepositoryImpl(jdbi, cudamiConfig);
    repo.setAgentEntityRepository(agentEntityRepositoryImpl);
    repo.setCollectionRepository(collectionRepositoryImpl);
    repo.setCorporateBodyRepository(corporateBodyRepositoryImpl);
    repo.setFileResourceMetadataRepository(fileResourceMetadataRepositoryImpl);
    repo.setGeolocationEntityRepositoryImpl(geoLocationEntityRepositoryImpl);
    repo.setGeoLocationRepositoryImpl(geoLocationRepositoryImpl);
    repo.setLinkedDataFileResourceRepository(linkedDataFileResourceRepository);
    repo.setPersonRepository(personRepositoryImpl);
  }

  @Test
  @DisplayName("can save and retrieve a DigitalObject with its directly embedded resources")
  void saveDigitalObject() throws RepositoryException {
    // Insert a license with uuid
    ensureLicense(EXISTING_LICENSE);

    // Insert a corporate body with UUID
    CorporateBody creator =
        CorporateBody.builder()
            .uuid(UUID.randomUUID())
            .label(Locale.GERMAN, "Körperschaft")
            .label(Locale.ENGLISH, "Corporate Body")
            .build();
    CorporateBodyRepositoryImpl corporateBodyRepository =
        new CorporateBodyRepositoryImpl(jdbi, cudamiConfig);
    corporateBodyRepository.save(creator);

    // Insert a geolocation with UUID
    GeoLocation creationPlace =
        GeoLocation.builder().uuid(UUID.randomUUID()).label(Locale.GERMAN, "Ort").build();
    GeoLocationRepositoryImpl geoLocationRepository =
        new GeoLocationRepositoryImpl(jdbi, cudamiConfig);
    geoLocationRepository.save(creationPlace);

    // Build a CreationInfo object with the formerly persisted contents
    CreationInfo creationInfo =
        CreationInfo.builder()
            .creator(creator)
            .date("2022-02-25")
            .geoLocation(creationPlace)
            .build();

    DigitalObject parent = DigitalObject.builder().label(Locale.GERMAN, "Parent").build();
    repo.save(parent);

    Tag tag = tagRepository.save(Tag.builder().value("foo").build());

    var noteText = new Text("a note to a digital object");
    var noteContent = new StructuredContent();
    var note = new LocalizedStructuredContent();
    noteContent.addContentBlock(noteText);
    note.put(Locale.forLanguageTag("und"), noteContent);

    DigitalObject digitalObject =
        DigitalObject.builder()
            .label(Locale.GERMAN, "deutschsprachiges Label")
            .label(Locale.ENGLISH, "english label")
            .description(Locale.GERMAN, "Beschreibung")
            .description(Locale.ENGLISH, "description")
            .license(EXISTING_LICENSE)
            .creationInfo(creationInfo)
            .parent(parent)
            .tag(tag)
            .note(note)
            .build();

    repo.save(digitalObject);

    // Verify, that the method-persisted DigitalObject is the same, which is in the database
    // Since some embeeded resource are not competely filled, we have to fill them explicitly
    DigitalObject persisted = repo.getByUuid(digitalObject.getUuid());
    if (persisted.getLicense() != null) {
      persisted.setLicense(licenseRepository.getByUuid(persisted.getLicense().getUuid()));
    }

    assertThat(persisted).isEqualToComparingFieldByField(digitalObject);
    assertThat(persisted.getLabel().getText(Locale.GERMAN)).isEqualTo("deutschsprachiges Label");
    assertThat(persisted.getLabel().getText(Locale.ENGLISH)).isEqualTo("english label");
    Paragraph paragraphDe =
        (Paragraph) persisted.getDescription().get(Locale.GERMAN).getContentBlocks().get(0);
    assertThat(((Text) paragraphDe.getContentBlocks().get(0)).getText()).isEqualTo("Beschreibung");

    assertThat(persisted.getLicense()).isEqualTo(EXISTING_LICENSE);

    assertThat(persisted.getCreationInfo().getCreator()).isEqualTo(creator);
    assertThat(persisted.getCreationInfo().getDate().format(DateTimeFormatter.ISO_DATE))
        .isEqualTo("2022-02-25");
    assertThat(persisted.getCreationInfo().getGeoLocation()).isEqualTo(creationPlace);

    assertThat(persisted.getParent()).isNotNull();
    assertThat(persisted.getParent().getUuid()).isEqualTo(parent.getUuid());
    assertThat(persisted.getParent().getLabel()).isEqualTo(parent.getLabel());

    assertThat(persisted.getTags().stream().map(Tag::getUuid).collect(Collectors.toList()))
        .containsExactly(tag.getUuid());
    assertThat(persisted.getNotes()).isNotEmpty().isEqualTo(digitalObject.getNotes());
  }

  @Test
  @DisplayName("returns the reduced DigitalObject without any creation info and embedded resources")
  void returnReduced() throws RepositoryException {
    DigitalObject digitalObject = buildDigitalObject();

    // The "save" method internally retrieves the object by findOne
    repo.save(digitalObject);

    PageResponse<DigitalObject> response =
        repo.find(PageRequest.builder().pageSize(1).pageNumber(0).build());
    assertThat(response).isNotNull();
    assertThat(response.getContent()).isNotEmpty();
    DigitalObject actualReduced = response.getContent().get(0);
    assertThat(actualReduced).isNotNull();
    assertThat(actualReduced.getLinkedDataResources()).isEmpty();
    assertThat(actualReduced.getRenderingResources()).isEmpty();
    assertThat(actualReduced.getCreationInfo()).isNull();
    assertThat(actualReduced.getLicense()).isNull();
    assertThat(actualReduced.getIdentifiableObjectType())
        .isEqualTo(IdentifiableObjectType.DIGITAL_OBJECT);
  }

  @Test
  @DisplayName("should return properly sized pages on search")
  void testSearchPageSize() {
    // Insert a bunch of DigitalObjects with labels
    IntStream.range(0, 20)
        .forEach(
            i -> {
              try {
                repo.save(
                    TestModelFixture.createDigitalObject(
                        Map.of(
                            Locale.GERMAN, "de labeltest" + i, Locale.ENGLISH, "en labeltest" + i),
                        Map.of(
                            Locale.GERMAN, "de desctest" + i, Locale.ENGLISH, "en desctest" + i)));
              } catch (InstantiationException
                  | IllegalAccessException
                  | IllegalArgumentException
                  | InvocationTargetException
                  | NoSuchMethodException
                  | SecurityException e) {
              } catch (RepositoryException e) {
                throw new RuntimeException(e);
              }
            });

    String query = "test";
    PageRequest pageRequest = new PageRequest();
    pageRequest.setPageSize(10);
    pageRequest.setPageNumber(0);
    pageRequest.setSearchTerm(query);
    pageRequest.setSorting(
        Sorting.builder()
            .order(
                de.digitalcollections.model.list.sorting.Order.builder()
                    .property("refId")
                    .direction(Direction.ASC)
                    .build())
            .build());

    PageResponse response = repo.find(pageRequest);
    assertThat(response.getExecutedSearchTerm()).isEqualTo(query);

    List<Identifiable> content = response.getContent();
    assertThat(content).hasSize(10);
  }

  @Test
  @DisplayName("can filter by the parent uuid")
  void filterByParentUuid()
      throws InstantiationException, IllegalAccessException, IllegalArgumentException,
          InvocationTargetException, NoSuchMethodException, SecurityException, RepositoryException {
    // Insert the parent DigitalObject
    DigitalObject parent =
        TestModelFixture.createDigitalObject(Map.of(Locale.GERMAN, "Parent"), Map.of());
    repo.save(parent);

    // Insert the ADO
    DigitalObject ado =
        TestModelFixture.createDigitalObject(Map.of(Locale.GERMAN, "ADO"), Map.of());
    ado.setParent(parent);
    repo.save(ado);

    // Retrieve the ADO by filtering the parent uuid
    PageRequest pageRequest = new PageRequest();
    pageRequest.setFiltering(
        new Filtering(
            List.of(new FilterCriterion("parent.uuid", FilterOperation.EQUALS, parent.getUuid()))));
    PageResponse response = repo.find(pageRequest);

    List<DigitalObject> actuals = response.getContent();
    assertThat(actuals).hasSize(1);

    DigitalObject actual = actuals.get(0);
    assertThat(actual.getUuid()).isNotEqualTo(parent.getUuid()); // Because actual is the ADO
    assertThat(actual.getParent().getUuid())
        .isEqualTo(
            parent.getUuid()); // Only the UUID of the parent is filled in a searchTerm result
  }

  @Test
  @DisplayName("returns all identifiers for a DigitalObject")
  void returnIdentifiers() throws RepositoryException {
    // Step1: Create the DigitalObject
    DigitalObject digitalObject = DigitalObject.builder().label(Locale.GERMAN, "Label").build();
    repo.save(digitalObject);

    // Step2: Create the identifiers and connect with with the DigitalObject
    Identifier identifier1 = new Identifier(digitalObject.getUuid(), "namespace1", "1");
    identifierRepositoryImpl.save(identifier1);
    Identifier identifier2 = new Identifier(digitalObject.getUuid(), "namespace2", "2");
    identifierRepositoryImpl.save(identifier2);

    // Step3: Create and persist an identifier for another DigitalObject
    DigitalObject otherDigitalObject =
        DigitalObject.builder().label(Locale.GERMAN, "Anderes Label").build();
    repo.save(otherDigitalObject);
    identifierRepositoryImpl.save(
        new Identifier(otherDigitalObject.getUuid(), "namespace1", "other"));

    // Verify, that we get only the two identifiers of the DigitalObject and not the one for the
    // other DigitalObject
    Identifier demandedIdentifier = new Identifier(null, "namespace1", "1");
    DigitalObject actual = repo.getByIdentifier(demandedIdentifier);

    assertThat(actual.getIdentifiers()).containsExactly(identifier1, identifier2);
  }

  @Test
  @DisplayName("can return null, when getByIdentifier finds no DigitalObject")
  void returnNullByGetByIdentifier() {
    assertThat(repo.getByIdentifier(new Identifier(null, "namespace", "nonexisting"))).isNull();
  }

  @Test
  @DisplayName("returns the partially filled DigitalObject by getByIdentifer")
  void returnGetByIdentifier() throws RepositoryException {
    DigitalObject digitalObject = buildDigitalObject();
    repo.save(digitalObject);
    identifierRepositoryImpl.save(new Identifier(digitalObject.getUuid(), "namespace", "key"));

    DigitalObject actual = repo.getByIdentifier(new Identifier(null, "namespace", "key"));

    CreationInfo actualCreationInfo = actual.getCreationInfo();
    assertThat(actualCreationInfo).isNotNull();
    assertThat(actualCreationInfo.getCreator().getLabel().getText(Locale.GERMAN))
        .isEqualTo("Körperschaft");
    assertThat(actual.getParent()).isNotNull();
  }

  @Test
  @DisplayName("save item UUID with digital object and retrieve it properly")
  void saveAndRetrieveItemUuid() throws RepositoryException {
    DigitalObject digitalObject = buildDigitalObject();
    Item item =
        Item.builder()
            .label(Locale.GERMAN, "Ein Buch")
            .exemplifiesManifestation(false)
            .identifier("mdz-sig", "Signatur")
            .title(Locale.GERMAN, "Ein Buchtitel")
            .build();
    itemRepository.save(item);
    assertThat(item.getUuid()).isNotNull();

    digitalObject.setItem(item);
    repo.save(digitalObject);
    assertThat(digitalObject.getUuid()).isNotNull();
    assertThat(digitalObject.getItem().getUuid()).isEqualTo(item.getUuid());
    DigitalObject retrieved = repo.getByUuid(digitalObject.getUuid());
    assertThat(retrieved.getItem())
        .isEqualTo(Item.builder().uuid(item.getUuid()).label(item.getLabel()).build());
  }

  @Test
  @Order(Integer.MAX_VALUE)
  @DisplayName("can return all label languages")
  void returnLanguages() throws RepositoryException {
    repo.save(DigitalObject.builder().label(Locale.GERMAN, "Test").build());
    repo.save(DigitalObject.builder().label(Locale.ENGLISH, "Test").build());

    List<Locale> allLanguages = repo.getLanguages();
    assertThat(allLanguages).containsAll(List.of(Locale.GERMAN, Locale.ENGLISH));

    DigitalObject digitalObject = buildDigitalObject();
    LocalizedText label = digitalObject.getLabel();
    label.put(Locale.KOREAN, "테스트");
    repo.save(digitalObject);
    List<Locale> languagesOfContainedDigitalObjects =
        repo.getLanguagesOfContainedDigitalObjects(digitalObject.getParent().getUuid());
    assertThat(languagesOfContainedDigitalObjects)
        .containsAll(List.of(Locale.GERMAN, Locale.ENGLISH, Locale.KOREAN));
  }

  @Test
  @DisplayName("can update a DigitalObject iwht its directly embedded resources")
  void update() throws RepositoryException {
    // Insert a license with uuid
    ensureLicense(EXISTING_LICENSE);

    // Insert a corporate body with UUID
    CorporateBody creator =
        CorporateBody.builder()
            .uuid(UUID.randomUUID())
            .label(Locale.GERMAN, "Körperschaft")
            .label(Locale.ENGLISH, "Corporate Body")
            .build();
    CorporateBodyRepositoryImpl corporateBodyRepository =
        new CorporateBodyRepositoryImpl(jdbi, cudamiConfig);
    corporateBodyRepository.save(creator);

    // Insert a geolocation with UUID
    GeoLocation creationPlace =
        GeoLocation.builder().uuid(UUID.randomUUID()).label(Locale.GERMAN, "Ort").build();
    GeoLocationRepositoryImpl geoLocationRepository =
        new GeoLocationRepositoryImpl(jdbi, cudamiConfig);
    geoLocationRepository.save(creationPlace);

    // Build a CreationInfo object with the formerly persisted contents
    CreationInfo creationInfo =
        CreationInfo.builder()
            .creator(creator)
            .date("2022-02-25")
            .geoLocation(creationPlace)
            .build();

    DigitalObject parent = DigitalObject.builder().label(Locale.GERMAN, "Parent").build();
    repo.save(parent);

    Tag tag = tagRepository.save(Tag.builder().value("foo").build());

    DigitalObject digitalObject =
        DigitalObject.builder()
            .label(Locale.GERMAN, "deutschsprachiges Label")
            .label(Locale.ENGLISH, "english label")
            .description(Locale.GERMAN, "Beschreibung")
            .description(Locale.ENGLISH, "description")
            .license(EXISTING_LICENSE)
            .creationInfo(creationInfo)
            .parent(parent)
            .tag(tag)
            .refId(42)
            .build();

    repo.save(digitalObject);

    digitalObject.setNumberOfBinaryResources(200);
    DigitalObject beforeUpdate = createDeepCopy(digitalObject);

    repo.update(digitalObject);
    assertThat(digitalObject.getLastModified()).isNotEqualTo(beforeUpdate.getLastModified());

    beforeUpdate.setLastModified(digitalObject.getLastModified());
    assertThat(digitalObject).isEqualToComparingFieldByField(beforeUpdate);

    // Verify, that the method-persisted DigitalObject is the same, which is in the database
    // Since some embeeded resource are not competely filled, we have to fill them explicitly
    DigitalObject persisted = repo.getByUuid(digitalObject.getUuid());
    if (persisted.getLicense() != null) {
      persisted.setLicense(licenseRepository.getByUuid(persisted.getLicense().getUuid()));
    }

    assertThat(digitalObject).isEqualToComparingFieldByField(persisted);
  }

  // -----------------------------------------------------------------
  private void ensureLicense(License license) {
    if (licenseRepository.getByUuid(license.getUuid()) == null) {
      licenseRepository.save(license);
    }
  }

  private DigitalObject buildDigitalObject() throws RepositoryException {
    // Insert a license with uuid
    ensureLicense(EXISTING_LICENSE);

    // Insert a corporate body with UUID
    CorporateBody creator =
        CorporateBody.builder()
            .uuid(UUID.randomUUID())
            .label(Locale.GERMAN, "Körperschaft")
            .label(Locale.ENGLISH, "Corporate Body")
            .build();
    CorporateBodyRepositoryImpl corporateBodyRepository =
        new CorporateBodyRepositoryImpl(jdbi, cudamiConfig);
    corporateBodyRepository.save(creator);

    // Insert a geolocation with UUID
    GeoLocation creationPlace =
        GeoLocation.builder().uuid(UUID.randomUUID()).label(Locale.GERMAN, "Ort").build();
    GeoLocationRepositoryImpl geoLocationRepository =
        new GeoLocationRepositoryImpl(jdbi, cudamiConfig);
    geoLocationRepository.save(creationPlace);

    // Insert a LinkedDataFileResource
    LinkedDataFileResource linkedDataFileResource =
        LinkedDataFileResource.builder()
            .uuid(UUID.randomUUID())
            .label(Locale.GERMAN, "Linked Data")
            .context("https://foo.bar/blubb.xml")
            .objectType("XML")
            .filename("blubb.xml") // required!!
            .mimeType(MimeType.MIME_APPLICATION_XML)
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
        CreationInfo.builder()
            .creator(creator)
            .date("2022-02-25")
            .geoLocation(creationPlace)
            .build();

    // Build a parent DigitalObject, save and retrieve it
    DigitalObject parent = DigitalObject.builder().label(Locale.GERMAN, "Parent").build();
    repo.save(parent);

    DigitalObject digitalObject =
        DigitalObject.builder()
            .label(Locale.GERMAN, "deutschsprachiges Label")
            .label(Locale.ENGLISH, "english label")
            .description(Locale.GERMAN, "Beschreibung")
            .description(Locale.ENGLISH, "description")
            .license(EXISTING_LICENSE)
            .creationInfo(creationInfo)
            .linkedDataFileResource(linkedDataFileResource)
            .renderingResource(renderingResource)
            .parent(parent)
            .build();
    return digitalObject;
  }
}
