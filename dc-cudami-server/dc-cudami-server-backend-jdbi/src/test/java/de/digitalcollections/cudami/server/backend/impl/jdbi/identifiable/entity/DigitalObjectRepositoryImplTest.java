package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import static de.digitalcollections.cudami.server.backend.impl.asserts.CudamiAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work.ItemRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.AbstractIdentifiableRepositoryImplTest;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifierRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent.AgentRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent.CorporateBodyRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent.PersonRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.geo.location.GeoLocationRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.geo.location.HumanSettlementRepositoryImpl;
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
import de.digitalcollections.model.validation.ValidationException;
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

  @Autowired private IdentifierRepository identifierRepository;
  @Autowired private UrlAliasRepository urlAliasRepository;

  @Autowired private CollectionRepositoryImpl collectionRepositoryImpl;

  @Autowired private CorporateBodyRepositoryImpl corporateBodyRepositoryImpl;

  @Autowired private AgentRepositoryImpl<Agent> agentRepositoryImpl;

  @Autowired
  private FileResourceMetadataRepositoryImpl<FileResource> fileResourceMetadataRepositoryImpl;

  @Autowired private TagRepositoryImpl tagRepository;

  @Autowired private GeoLocationRepositoryImpl<GeoLocation> geoLocationRepositoryImpl;
  @Autowired private HumanSettlementRepositoryImpl humanSettlementRepositoryImpl;

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
    repo =
        new DigitalObjectRepositoryImpl(
            jdbi,
            cudamiConfig,
            iiifServerConfig,
            identifierRepository,
            urlAliasRepository,
            iiifObjectMapper);
    repo.setAgentRepository(agentRepositoryImpl);
    repo.setCollectionRepository(collectionRepositoryImpl);
    repo.setCorporateBodyRepository(corporateBodyRepositoryImpl);
    repo.setFileResourceMetadataRepository(fileResourceMetadataRepositoryImpl);
    repo.setGeoLocationRepositoryImpl(geoLocationRepositoryImpl);
    repo.setHumanSettlementRepository(humanSettlementRepositoryImpl);
    repo.setLinkedDataFileResourceRepository(linkedDataFileResourceRepository);
    repo.setPersonRepository(personRepositoryImpl);
  }

  @Test
  @DisplayName("can save and retrieve a DigitalObject with its directly embedded resources")
  void saveDigitalObject() throws RepositoryException, ValidationException {
    // Insert a license with uuid
    ensureLicense(EXISTING_LICENSE);

    // Insert a corporate body with UUID
    CorporateBody creator =
        CorporateBody.builder()
            .uuid(UUID.randomUUID())
            .label(Locale.GERMAN, "Körperschaft")
            .label(Locale.ENGLISH, "Corporate Body")
            .identifier(Identifier.builder().namespace("CB").id("corporation").build())
            .build();
    CorporateBodyRepositoryImpl corporateBodyRepository =
        new CorporateBodyRepositoryImpl(
            jdbi, cudamiConfig, identifierRepository, urlAliasRepository);
    corporateBodyRepository.save(creator);

    // Insert a geolocation with UUID
    GeoLocation creationPlace =
        GeoLocation.builder().uuid(UUID.randomUUID()).label(Locale.GERMAN, "Ort").build();
    GeoLocationRepositoryImpl geoLocationRepository =
        new GeoLocationRepositoryImpl(jdbi, cudamiConfig, identifierRepository, urlAliasRepository);
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

    Tag tag = Tag.builder().value("foo").build();
    tagRepository.save(tag);

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
  void returnReduced() throws RepositoryException, ValidationException {
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
  void testSearchPageSize() throws RepositoryException, ValidationException {
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
                  | SecurityException
                  | ValidationException e) {
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
      throws InstantiationException,
          IllegalAccessException,
          IllegalArgumentException,
          InvocationTargetException,
          NoSuchMethodException,
          SecurityException,
          RepositoryException,
          ValidationException {
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
            List.of(
                new FilterCriterion(
                    "parent_uuid", true, FilterOperation.EQUALS, parent.getUuid()))));
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
  void returnIdentifiers() throws RepositoryException, ValidationException {
    // Step1: Create and persist a DigitalObject with two identifiers
    DigitalObject digitalObject = DigitalObject.builder().label(Locale.GERMAN, "Label").build();
    Identifier identifier1 = Identifier.builder().namespace("namespace1").id("1").build();
    digitalObject.addIdentifier(identifier1);
    Identifier identifier2 = Identifier.builder().namespace("namespace2").id("2").build();
    digitalObject.addIdentifier(identifier2);
    repo.save(digitalObject);

    // Step2: Create and persist an identifier for another DigitalObject
    DigitalObject otherDigitalObject =
        DigitalObject.builder().label(Locale.GERMAN, "Anderes Label").build();
    otherDigitalObject.addIdentifier(
        Identifier.builder().namespace("namespace1").id("other").build());
    repo.save(otherDigitalObject);

    // Verify, that we get only the two identifiers of the DigitalObject and not the one for the
    // other DigitalObject
    Identifier demandedIdentifier = Identifier.builder().namespace("namespace1").id("1").build();
    DigitalObject actual = repo.getByIdentifier(demandedIdentifier);

    assertThat(actual.getIdentifiers()).containsExactly(identifier1, identifier2);
  }

  @Test
  @DisplayName("can return null, when getByIdentifier finds no DigitalObject")
  void returnNullByGetByIdentifier() throws RepositoryException {
    assertThat(
            repo.getByIdentifier(
                Identifier.builder().namespace("namespace").id("nonexisting").build()))
        .isNull();
  }

  @Test
  @DisplayName("returns the partially filled DigitalObject by getByIdentifer")
  void returnGetByIdentifier() throws RepositoryException, ValidationException {
    DigitalObject digitalObject = buildDigitalObject();
    digitalObject.addIdentifier(Identifier.builder().namespace("namespace").id("key").build());
    repo.save(digitalObject);

    DigitalObject actual =
        repo.getByIdentifier(Identifier.builder().namespace("namespace").id("key").build());

    CreationInfo actualCreationInfo = actual.getCreationInfo();
    assertThat(actualCreationInfo).isNotNull();
    assertThat(actualCreationInfo.getCreator().getLabel().getText(Locale.GERMAN))
        .isEqualTo("Körperschaft");
    assertThat(actual.getParent()).isNotNull();
  }

  @Test
  @DisplayName("save item UUID with digital object and retrieve it properly")
  void saveAndRetrieveItemUuid() throws RepositoryException, ValidationException {
    DigitalObject digitalObject = buildDigitalObject();
    Item item =
        Item.builder()
            .label(Locale.GERMAN, "Ein Buch")
            .exemplifiesManifestation(false)
            .identifier(Identifier.builder().namespace("mdz-sig").id("Signatur").build())
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
        .isEqualTo(
            Item.builder()
                .uuid(item.getUuid())
                .label(item.getLabel())
                .identifier(Identifier.builder().namespace("mdz-sig").id("Signatur").build())
                .build());
  }

  @Test
  @Order(Integer.MAX_VALUE)
  @DisplayName("can return all label languages")
  void returnLanguages() throws RepositoryException, ValidationException {
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

  /**
   * @throws RepositoryException
   */
  @Test
  @DisplayName("can update a DigitalObject with its directly embedded resources")
  void update() throws RepositoryException, ValidationException {
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
        new CorporateBodyRepositoryImpl(
            jdbi, cudamiConfig, identifierRepository, urlAliasRepository);
    corporateBodyRepository.save(creator);

    // Insert a geolocation with UUID
    GeoLocation creationPlace =
        GeoLocation.builder().uuid(UUID.randomUUID()).label(Locale.GERMAN, "Ort").build();
    GeoLocationRepositoryImpl geoLocationRepository =
        new GeoLocationRepositoryImpl(jdbi, cudamiConfig, identifierRepository, urlAliasRepository);
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

    Tag tag = Tag.builder().value("foo").build();
    tagRepository.save(tag);

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

  @Test
  @DisplayName("can return an empty set of connected digital objects for an null item")
  void digitalObjectsForNullItem() throws RepositoryException {
    PageRequest pageRequest = PageRequest.builder().pageSize(25).pageNumber(0).build();
    assertThat(repo.findDigitalObjectsByItem((UUID) null, pageRequest)).isEmpty();
  }

  @Test
  @DisplayName("can return an empty set of connected digital objects for an nonexisting item")
  void digitalObjectsForNonexistingItem() throws RepositoryException {
    PageRequest pageRequest = PageRequest.builder().pageSize(25).pageNumber(0).build();
    assertThat(repo.findDigitalObjectsByItem(UUID.randomUUID(), pageRequest)).isEmpty();
  }

  @Test
  @DisplayName(
      "can return an empty set of connected digital objects for an item which has no digital objects connected to it")
  void digitalObjectsForItemWithoutDigitalObjects()
      throws RepositoryException, ValidationException {
    PageRequest pageRequest = PageRequest.builder().pageSize(25).pageNumber(0).build();
    Item item = Item.builder().label("item without digital objects").build();
    itemRepository.save(item);
    DigitalObject digitalObject =
        DigitalObject.builder().label("digital object without item").build();
    repo.save(digitalObject);

    assertThat(repo.findDigitalObjectsByItem(item.getUuid(), pageRequest)).isEmpty();
  }

  @Test
  @DisplayName("can return digital objects connected to an item")
  void digitalObjectsForItem() throws RepositoryException, ValidationException {
    PageRequest pageRequest = PageRequest.builder().pageSize(25).pageNumber(0).build();
    Item item1 = Item.builder().label("item1 with two digitalObject2").build();
    itemRepository.save(item1);
    Item item2 = Item.builder().label("item2 with one digitalObject").build();
    itemRepository.save(item2);
    DigitalObject digitalObject1 =
        DigitalObject.builder().label("digital object 1 for item1").item(item1).build();
    repo.save(digitalObject1);
    DigitalObject digitalObject2 =
        DigitalObject.builder().label("digital object 2 for item1").item(item1).build();
    repo.save(digitalObject2);
    DigitalObject digitalObject3 =
        DigitalObject.builder().label("digital object 1 for item2").item(item2).build();
    repo.save(digitalObject3);

    PageResponse<DigitalObject> actual =
        repo.findDigitalObjectsByItem(item1.getUuid(), pageRequest);
    // The Item contained in the DigitalObjects has only UUID, Identifiers and Label set.
    // Therefore we must "adjust" `item1`
    Item containedItem = Item.builder().uuid(item1.getUuid()).label(item1.getLabel()).build();
    digitalObject1.setItem(containedItem);
    digitalObject2.setItem(containedItem);
    assertThat(actual.getContent()).containsExactlyInAnyOrder(digitalObject1, digitalObject2);
  }

  @Test
  @DisplayName("can use paging on retrieval of digital objects connected to an item")
  void pagedDigitalObjectsForItem() throws RepositoryException, ValidationException {
    PageRequest pageRequest = PageRequest.builder().pageSize(1).pageNumber(0).build();
    Item item = Item.builder().label("item1 with two digitalObject2").build();
    itemRepository.save(item);
    DigitalObject digitalObject1 =
        DigitalObject.builder().label("digital object 1 for item1").item(item).build();
    repo.save(digitalObject1);
    DigitalObject digitalObject2 =
        DigitalObject.builder().label("digital object 2 for item1").item(item).build();
    repo.save(digitalObject2);

    PageResponse<DigitalObject> actual = repo.findDigitalObjectsByItem(item.getUuid(), pageRequest);
    assertThat(actual.getContent()).hasSize(1);
  }

  // -----------------------------------------------------------------
  private void ensureLicense(License license) throws RepositoryException, ValidationException {
    if (licenseRepository.getByUuid(license.getUuid()) == null) {
      licenseRepository.save(license);
    }
  }

  private DigitalObject buildDigitalObject() throws RepositoryException, ValidationException {
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
        new CorporateBodyRepositoryImpl(
            jdbi, cudamiConfig, identifierRepository, urlAliasRepository);
    corporateBodyRepository.save(creator);

    // Insert a geolocation with UUID
    GeoLocation creationPlace =
        GeoLocation.builder().uuid(UUID.randomUUID()).label(Locale.GERMAN, "Ort").build();
    GeoLocationRepositoryImpl geoLocationRepository =
        new GeoLocationRepositoryImpl(jdbi, cudamiConfig, identifierRepository, urlAliasRepository);
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
        new FileResourceMetadataRepositoryImpl<FileResource>(
            jdbi, cudamiConfig, identifierRepository, urlAliasRepository);
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
