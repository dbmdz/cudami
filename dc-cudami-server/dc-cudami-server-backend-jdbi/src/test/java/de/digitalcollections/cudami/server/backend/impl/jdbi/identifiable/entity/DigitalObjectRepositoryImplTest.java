package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import static de.digitalcollections.cudami.server.backend.impl.asserts.CudamiAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work.ItemRepository;
import de.digitalcollections.cudami.server.backend.impl.database.config.SpringConfigBackendDatabase;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifierRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent.CorporateBodyRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent.PersonRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.geo.location.GeoLocationRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource.FileResourceMetadataRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource.LinkedDataFileResourceRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.legal.LicenseRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.model.TestModelFixture;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.IdentifiableObjectType;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocation;
import de.digitalcollections.model.identifiable.entity.work.Item;
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
import de.digitalcollections.model.production.CreationInfo;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.text.contentblock.Paragraph;
import de.digitalcollections.model.text.contentblock.Text;
import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    webEnvironment = WebEnvironment.MOCK,
    classes = {DigitalObjectRepositoryImpl.class})
@ContextConfiguration(classes = SpringConfigBackendDatabase.class)
@DisplayName("The DigitalObject Repository")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DigitalObjectRepositoryImplTest {

  DigitalObjectRepositoryImpl repo;

  @Autowired PostgreSQLContainer postgreSQLContainer;

  @Autowired Jdbi jdbi;

  @Autowired private CollectionRepositoryImpl collectionRepositoryImpl;

  @Autowired private CorporateBodyRepositoryImpl corporateBodyRepositoryImpl;

  @Autowired private EntityRepositoryImpl<Agent> agentEntityRepositoryImpl;

  @Autowired
  private FileResourceMetadataRepositoryImpl<FileResource> fileResourceMetadataRepositoryImpl;

  @Autowired CudamiConfig cudamiConfig;

  @Autowired private EntityRepositoryImpl<GeoLocation> geoLocationEntityRepositoryImpl;

  @Autowired private GeoLocationRepositoryImpl geoLocationRepositoryImpl;

  @Autowired private IdentifierRepositoryImpl identifierRepositoryImpl;

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
  @DisplayName("is testable")
  void containerIsUpAndRunning() {
    assertThat(postgreSQLContainer.isRunning()).isTrue();
  }

  @Test
  @DisplayName("can save and retrieve a DigitalObject with its directly embedded resources")
  void saveDigitalObject() {
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

    DigitalObject parent =
        repo.save(DigitalObject.builder().label(Locale.GERMAN, "Parent").build());

    DigitalObject digitalObject =
        DigitalObject.builder()
            .label(Locale.GERMAN, "deutschsprachiges Label")
            .label(Locale.ENGLISH, "english label")
            .description(Locale.GERMAN, "Beschreibung")
            .description(Locale.ENGLISH, "description")
            .license(EXISTING_LICENSE)
            .creationInfo(creationInfo)
            .parent(parent)
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

    assertThat(actual.getParent()).isNotNull();
    assertThat(actual.getParent().getUuid()).isEqualTo(parent.getUuid());
    assertThat(actual.getParent().getLabel()).isEqualTo(parent.getLabel());
  }

  @Test
  @DisplayName("returns the reduced DigitalObject without any creation info and embedded resources")
  void returnReduced() {
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
              repo.save(
                  TestModelFixture.createDigitalObject(
                      Map.of(Locale.GERMAN, "de labeltest" + i, Locale.ENGLISH, "en labeltest" + i),
                      Map.of(Locale.GERMAN, "de desctest" + i, Locale.ENGLISH, "en desctest" + i)));
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
  void filterByParentUuid() {
    // Insert the parent DigitalObject
    DigitalObject parent =
        TestModelFixture.createDigitalObject(Map.of(Locale.GERMAN, "Parent"), Map.of());
    parent = repo.save(parent);

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
    DigitalObject persisted = repo.save(digitalObject);

    // Step2: Create the identifiers and connect with with the DigitalObject
    Identifier identifier1 =
        identifierRepositoryImpl.save(new Identifier(persisted.getUuid(), "namespace1", "1"));
    Identifier identifier2 =
        identifierRepositoryImpl.save(new Identifier(persisted.getUuid(), "namespace2", "2"));

    // Step3: Create and persist an identifier for another DigitalObject
    DigitalObject otherDigitalObject =
        DigitalObject.builder().label(Locale.GERMAN, "Anderes Label").build();
    DigitalObject otherPersisted = repo.save(otherDigitalObject);
    identifierRepositoryImpl.save(new Identifier(otherPersisted.getUuid(), "namespace1", "other"));

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
    digitalObject = repo.save(digitalObject);
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
  void saveAndRetrieveItemUuid() {
    DigitalObject digitalObject = buildDigitalObject();
    Item item =
        Item.builder()
            .label(Locale.GERMAN, "Ein Buch")
            .exemplifiesManifestation(false)
            .identifier("mdz-sig", "Signatur")
            .title(Locale.GERMAN, "Ein Buchtitel")
            .build();
    Item savedItem = itemRepository.save(item);
    assertThat(savedItem.getUuid()).isNotNull();

    digitalObject.setItem(savedItem);
    DigitalObject savedDigitalObject = repo.save(digitalObject);
    assertThat(savedDigitalObject.getUuid()).isNotNull();
    assertThat(savedDigitalObject.getItem().getUuid()).isEqualTo(savedItem.getUuid());
    DigitalObject retrieved = repo.getByUuid(savedDigitalObject.getUuid());
    assertThat(retrieved.getItem()).isEqualTo(Item.builder().uuid(savedItem.getUuid()).build());
  }

  @Test
  @Order(Integer.MAX_VALUE)
  @DisplayName("")
  void returnLanguages() {
    List<Locale> allLanguages = repo.getLanguages();
    assertThat(allLanguages).containsAll(List.of(Locale.GERMAN, Locale.ENGLISH));

    DigitalObject digitalObject = buildDigitalObject();
    LocalizedText label = digitalObject.getLabel();
    label.put(Locale.KOREAN, "테스트");
    digitalObject = repo.save(digitalObject);
    List<Locale> languagesOfContainedDigitalObjects =
        repo.getLanguagesOfContainedDigitalObjects(digitalObject.getParent().getUuid());
    assertThat(languagesOfContainedDigitalObjects)
        .containsAll(List.of(Locale.GERMAN, Locale.ENGLISH, Locale.KOREAN));
  }

  // -----------------------------------------------------------------
  private void ensureLicense(License license) {
    LicenseRepositoryImpl licenseRepository = new LicenseRepositoryImpl(jdbi, cudamiConfig);
    if (licenseRepository.getByUuid(license.getUuid()) == null) {
      licenseRepository.save(license);
    }
  }

  private DigitalObject buildDigitalObject() {
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
    DigitalObject parent =
        repo.save(DigitalObject.builder().label(Locale.GERMAN, "Parent").build());

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
