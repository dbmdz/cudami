package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.impl.database.config.SpringConfigBackendDatabase;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.DigitalObjectRepositoryImpl;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.resource.TextFileResource;
import java.util.List;
import java.util.Locale;
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
import org.testcontainers.containers.PostgreSQLContainer;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    webEnvironment = WebEnvironment.MOCK,
    classes = {DigitalObjectRenderingFileResourceRepositoryImpl.class})
@ContextConfiguration(classes = SpringConfigBackendDatabase.class)
@Sql(scripts = "classpath:cleanup_database.sql")
@DisplayName("The DigitalObjectRenderingFileResource Repository")
class DigitalObjectRenderingFileResourceRepositoryImplTest {

  DigitalObjectRenderingFileResourceRepositoryImpl repo;
  @Autowired private DigitalObjectRepositoryImpl digitalObjectRepository;

  @Autowired private TextFileResourceRepositoryImpl textFileResourceMetadataRepository;

  @Autowired CudamiConfig cudamiConfig;

  @Autowired PostgreSQLContainer postgreSQLContainer;

  @Autowired
  private FileResourceMetadataRepositoryImpl<FileResource> renderingFileResourceRepositoryImpl;

  @Autowired Jdbi jdbi;

  @BeforeEach
  public void beforeEach() {
    repo =
        new DigitalObjectRenderingFileResourceRepositoryImpl(
            jdbi, cudamiConfig, renderingFileResourceRepositoryImpl);
  }

  @DisplayName("can delete a list of RenderingResources by their uuids")
  @Test
  void delete() {
    // Persist the DigitalObject
    DigitalObject digitalObject =
        DigitalObject.builder()
            .label(Locale.GERMAN, "deutschsprachiges Label")
            .label(Locale.ENGLISH, "english label")
            .description(Locale.GERMAN, "Beschreibung")
            .description(Locale.ENGLISH, "description")
            .build();
    digitalObject = digitalObjectRepository.save(digitalObject);

    // Persist the RenderingFileResource
    TextFileResource renderingFileResource =
        TextFileResource.builder()
            .label(Locale.GERMAN, "Linked Data")
            .filename("blubb.xml") // required!!
            .mimeType(MimeType.MIME_APPLICATION_XML)
            .build();
    TextFileResource persistedRenderingResource =
        textFileResourceMetadataRepository.save(renderingFileResource);

    repo.saveRenderingFileResources(digitalObject.getUuid(), List.of(persistedRenderingResource));
    List<FileResource> persisted = repo.getRenderingFileResources(digitalObject.getUuid());

    repo.delete(persisted.stream().map(FileResource::getUuid).collect(Collectors.toList()));

    List<FileResource> actual = repo.getRenderingFileResources(digitalObject.getUuid());
    assertThat(actual).isEmpty();
  }
}
