package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.impl.database.config.SpringConfigBackendDatabase;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.DigitalObjectRepositoryImpl;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.resource.LinkedDataFileResource;
import java.util.List;
import java.util.Locale;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    webEnvironment = WebEnvironment.MOCK,
    classes = {DigitalObjectLinkedDataFileResourceRepositoryImpl.class})
@ContextConfiguration(classes = SpringConfigBackendDatabase.class)
@Sql(scripts = "classpath:cleanup_database.sql")
@DisplayName("The DigitalObjectLinkedDataFileResource Repository")
class DigitalObjectLinkedDataFileResourceRepositoryImplTest {

  DigitalObjectLinkedDataFileResourceRepositoryImpl repo;

  @Autowired CudamiConfig cudamiConfig;

  @Autowired PostgreSQLContainer postgreSQLContainer;

  @Autowired private DigitalObjectRepositoryImpl digitalObjectRepository;
  @Autowired private LinkedDataFileResourceRepositoryImpl linkedDataFileResourceRepositoryImpl;

  @Autowired Jdbi jdbi;

  @BeforeEach
  public void beforeEach() {
    repo =
        new DigitalObjectLinkedDataFileResourceRepositoryImpl(
            jdbi, cudamiConfig, linkedDataFileResourceRepositoryImpl);
  }

  @DisplayName("can save LinkedDataFileResources for a DigitalObject")
  @Test
  void setLinkedDataFileResourcesForDigitalObject() {
    // Persist the DigitalObject
    DigitalObject digitalObject =
        DigitalObject.builder()
            .withLabel(Locale.GERMAN, "deutschsprachiges Label")
            .withLabel(Locale.ENGLISH, "english label")
            .withDescription(Locale.GERMAN, "Beschreibung")
            .withDescription(Locale.ENGLISH, "description")
            .build();
    digitalObject = digitalObjectRepository.save(digitalObject);

    // Try to persist the LinkedDataFileResource
    LinkedDataFileResource linkedDataFileResource =
        LinkedDataFileResource.builder()
            .withLabel(Locale.GERMAN, "Linked Data")
            .withContext("https://foo.bar/blubb.xml")
            .withObjectType("XML")
            .withFilename("blubb.xml") // required!!
            .withMimeType(MimeType.MIME_APPLICATION_XML)
            .build();

    List<LinkedDataFileResource> actual =
        repo.setLinkedDataFileResources(digitalObject.getUuid(), List.of(linkedDataFileResource));

    assertThat(actual).hasSize(1);
    assertThat(actual.get(0).getUuid()).isNotNull();
  }

  @DisplayName("can retrieve LinkedDataFileResources for a DigitalObject")
  @Test
  void getLinkedDataFileResourcesForDigitalObject() {
    // Persist the DigitalObject
    DigitalObject digitalObject =
        DigitalObject.builder()
            .withLabel(Locale.GERMAN, "deutschsprachiges Label")
            .withLabel(Locale.ENGLISH, "english label")
            .withDescription(Locale.GERMAN, "Beschreibung")
            .withDescription(Locale.ENGLISH, "description")
            .build();
    digitalObject = digitalObjectRepository.save(digitalObject);

    // Try to persist the LinkedDataFileResource
    LinkedDataFileResource linkedDataFileResource =
        LinkedDataFileResource.builder()
            .withLabel(Locale.GERMAN, "Linked Data")
            .withContext("https://foo.bar/blubb.xml")
            .withObjectType("XML")
            .withFilename("blubb.xml") // required!!
            .withMimeType(MimeType.MIME_APPLICATION_XML)
            .build();
    List<LinkedDataFileResource> persisted =
        repo.setLinkedDataFileResources(digitalObject.getUuid(), List.of(linkedDataFileResource));

    List<LinkedDataFileResource> actual = repo.getLinkedDataFileResources(digitalObject.getUuid());

    assertThat(actual).isNotEmpty();
    assertThat(actual).isEqualTo(persisted);
  }
}
