package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.impl.database.config.SpringConfigBackendTestDatabase;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.DigitalObjectRepositoryImpl;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.resource.LinkedDataFileResource;
import de.digitalcollections.model.validation.ValidationException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
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

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    webEnvironment = WebEnvironment.MOCK,
    classes = {DigitalObjectLinkedDataFileResourceRepositoryImpl.class})
@ContextConfiguration(classes = SpringConfigBackendTestDatabase.class)
@Sql(scripts = "classpath:cleanup_database.sql")
@DisplayName("The DigitalObjectLinkedDataFileResource Repository")
class DigitalObjectLinkedDataFileResourceRepositoryImplTest {

  DigitalObjectLinkedDataFileResourceRepositoryImpl repo;

  @Autowired CudamiConfig cudamiConfig;

  @Autowired private DigitalObjectRepositoryImpl digitalObjectRepository;
  @Autowired private LinkedDataFileResourceRepositoryImpl linkedDataFileResourceRepositoryImpl;

  @Autowired Jdbi jdbi;

  @BeforeEach
  public void beforeEach() {
    repo =
        new DigitalObjectLinkedDataFileResourceRepositoryImpl(
            jdbi, cudamiConfig, linkedDataFileResourceRepositoryImpl);
  }

  @DisplayName("can save and retrieve LinkedDataFileResources for a DigitalObject")
  @Test
  void setLinkedDataFileResourcesForDigitalObject()
      throws RepositoryException, ValidationException {
    // Persist the DigitalObject
    DigitalObject digitalObject =
        DigitalObject.builder()
            .label(Locale.GERMAN, "deutschsprachiges Label")
            .label(Locale.ENGLISH, "english label")
            .description(Locale.GERMAN, "Beschreibung")
            .description(Locale.ENGLISH, "description")
            .build();
    digitalObjectRepository.save(digitalObject);

    // Try to persist the LinkedDataFileResource
    LinkedDataFileResource linkedDataFileResource =
        LinkedDataFileResource.builder()
            .label(Locale.GERMAN, "Linked Data")
            .context("https://foo.bar/blubb.xml")
            .objectType("XML")
            .filename("blubb.xml") // required!!
            .mimeType(MimeType.MIME_APPLICATION_XML)
            .build();

    repo.setLinkedDataFileResources(digitalObject.getUuid(), List.of(linkedDataFileResource));
    List<LinkedDataFileResource> actual = repo.getLinkedDataFileResources(digitalObject.getUuid());

    assertThat(actual).hasSize(1);
    assertThat(actual.get(0).getUuid()).isNotNull();
  }

  @DisplayName("can delete a list of LinkedDataFileResources by their uuids")
  @Test
  void delete() throws RepositoryException, ValidationException {
    // Persist the DigitalObject
    DigitalObject digitalObject =
        DigitalObject.builder()
            .label(Locale.GERMAN, "deutschsprachiges Label")
            .label(Locale.ENGLISH, "english label")
            .description(Locale.GERMAN, "Beschreibung")
            .description(Locale.ENGLISH, "description")
            .build();
    digitalObjectRepository.save(digitalObject);

    // Persist the LinkedDataFileResource
    LinkedDataFileResource linkedDataFileResource =
        LinkedDataFileResource.builder()
            .label(Locale.GERMAN, "Linked Data")
            .context("https://foo.bar/blubb.xml")
            .objectType("XML")
            .filename("blubb.xml") // required!!
            .mimeType(MimeType.MIME_APPLICATION_XML)
            .build();
    repo.setLinkedDataFileResources(digitalObject.getUuid(), List.of(linkedDataFileResource));
    List<LinkedDataFileResource> persisted =
        repo.getLinkedDataFileResources(digitalObject.getUuid());

    repo.delete(
        persisted.stream().map(LinkedDataFileResource::getUuid).collect(Collectors.toList()));

    List<LinkedDataFileResource> actual = repo.getLinkedDataFileResources(digitalObject.getUuid());
    assertThat(actual).isEmpty();
  }

  @DisplayName(
      "can count the number of entries for a provided LinkedDataFileResource uuid when no entries exist")
  @Test
  void countZero() {
    assertThat(repo.countDigitalObjectsForResource(UUID.randomUUID())).isEqualTo(0);
  }

  @DisplayName(
      "can count the number of entries for a provided LinkedDataFileResource uuid when entries exist")
  @Test
  void countMoreThanZero() throws RepositoryException, ValidationException {
    // Persist the DigitalObject
    DigitalObject digitalObject =
        DigitalObject.builder()
            .label(Locale.GERMAN, "deutschsprachiges Label")
            .label(Locale.ENGLISH, "english label")
            .description(Locale.GERMAN, "Beschreibung")
            .description(Locale.ENGLISH, "description")
            .build();
    digitalObjectRepository.save(digitalObject);

    // Persist the LinkedDataFileResource
    LinkedDataFileResource linkedDataFileResource =
        LinkedDataFileResource.builder()
            .label(Locale.GERMAN, "Linked Data")
            .context("https://foo.bar/blubb.xml")
            .objectType("XML")
            .filename("blubb.xml") // required!!
            .mimeType(MimeType.MIME_APPLICATION_XML)
            .build();
    repo.setLinkedDataFileResources(digitalObject.getUuid(), List.of(linkedDataFileResource));

    assertThat(repo.countDigitalObjectsForResource(linkedDataFileResource.getUuid())).isEqualTo(1);
  }

  @DisplayName("returns zero, when nothing was deleted")
  @Test
  void noDeletionReturnsZero() throws RepositoryException {
    assertThat(repo.delete(UUID.randomUUID())).isEqualTo(0);
  }

  @DisplayName("returns the number of deleted items")
  @Test
  void deletionReturnsNumberOfDeletedItems() throws RepositoryException, ValidationException {
    // Persist the DigitalObject
    DigitalObject digitalObject =
        DigitalObject.builder()
            .label(Locale.GERMAN, "deutschsprachiges Label")
            .label(Locale.ENGLISH, "english label")
            .description(Locale.GERMAN, "Beschreibung")
            .description(Locale.ENGLISH, "description")
            .build();
    digitalObjectRepository.save(digitalObject);

    // Persist the LinkedDataFileResource
    LinkedDataFileResource linkedDataFileResource =
        LinkedDataFileResource.builder()
            .label(Locale.GERMAN, "Linked Data")
            .context("https://foo.bar/blubb.xml")
            .objectType("XML")
            .filename("blubb.xml") // required!!
            .mimeType(MimeType.MIME_APPLICATION_XML)
            .build();
    repo.setLinkedDataFileResources(digitalObject.getUuid(), List.of(linkedDataFileResource));

    assertThat(repo.delete(linkedDataFileResource.getUuid())).isEqualTo(1);
  }
}
