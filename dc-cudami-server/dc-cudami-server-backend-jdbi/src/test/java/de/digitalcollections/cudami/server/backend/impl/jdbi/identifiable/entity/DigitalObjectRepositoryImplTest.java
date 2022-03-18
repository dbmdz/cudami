package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import static de.digitalcollections.cudami.server.backend.impl.asserts.CudamiAssertions.assertThat;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.impl.database.config.SpringConfigBackendDatabase;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent.CorporateBodyRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent.PersonRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.geo.location.GeoLocationRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource.FileResourceMetadataRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource.LinkedDataFileResourceRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.legal.LicenseRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.model.TestModelFixture;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.DigitalObjectBuilder;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBodyBuilder;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocation;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocationBuilder;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.resource.LinkedDataFileResource;
import de.digitalcollections.model.identifiable.resource.LinkedDataFileResourceBuilder;
import de.digitalcollections.model.legal.License;
import de.digitalcollections.model.legal.LicenseBuilder;
import de.digitalcollections.model.paging.Direction;
import de.digitalcollections.model.paging.OrderBuilder;
import de.digitalcollections.model.paging.PageRequestBuilder;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.paging.Sorting;
import de.digitalcollections.model.production.CreationInfo;
import de.digitalcollections.model.production.CreationInfoBuilder;
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
import org.junit.jupiter.api.Test;
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

  @Autowired private LinkedDataFileResourceRepositoryImpl linkedDataFileResourceRepository;

  @Autowired private PersonRepositoryImpl personRepositoryImpl;

  private static final License EXISTING_LICENSE =
      new LicenseBuilder()
          .withUuid(UUID.randomUUID())
          .withAcronym("CC0 1.0")
          .withUrl("http://rightsstatements.org/vocab/NoC-NC/1.0/")
          .withLabel(
              Locale.GERMAN, "Kein Urheberrechtsschutz – nur nicht-kommerzielle Nutzung erlaubt")
          .withLabel(Locale.ENGLISH, "No Copyright – Non-Commercial Use Only")
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

  @Test
  @DisplayName("returns the reduced DigitalObject without any creation info and embedded resources")
  void returnReduced() {
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

    // Build a parent DigitalObject, save and retrieve it
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
            .build();

    // The "save" method internally retrieves the object by findOne
    repo.save(digitalObject);

    PageResponse<DigitalObject> response =
        repo.find(new PageRequestBuilder().pageSize(1).pageNumber(0).build());
    assertThat(response).isNotNull();
    assertThat(response.getContent()).isNotEmpty();
    DigitalObject actualReduced = response.getContent().get(0);
    assertThat(actualReduced).isNotNull();
    assertThat(actualReduced.getLinkedDataResources()).isEmpty();
    assertThat(actualReduced.getRenderingResources()).isEmpty();
    assertThat(actualReduced.getCreationInfo()).isNull();
    assertThat(actualReduced.getLicense()).isNull();
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
    SearchPageRequest searchPageRequest = new SearchPageRequest();
    searchPageRequest.setPageSize(10);
    searchPageRequest.setPageNumber(0);
    searchPageRequest.setQuery(query);
    searchPageRequest.setSorting(
        Sorting.defaultBuilder()
            .order(new OrderBuilder().property("refId").direction(Direction.ASC).build())
            .build());

    SearchPageResponse response = repo.find(searchPageRequest);
    assertThat(((SearchPageRequest) response.getPageRequest()).getQuery()).isEqualTo(query);

    List<Identifiable> content = response.getContent();
    assertThat(content).hasSize(10);
  }

  // -----------------------------------------------------------------
  private void ensureLicense(License license) {
    LicenseRepositoryImpl licenseRepository = new LicenseRepositoryImpl(jdbi, cudamiConfig);
    if (licenseRepository.getByUuid(license.getUuid()) == null) {
      licenseRepository.save(license);
    }
  }
}
