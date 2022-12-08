package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.impl.database.config.SpringConfigBackendTestDatabase;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.web.WebpageRepositoryImpl;
import de.digitalcollections.model.identifiable.entity.EntityType;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import de.digitalcollections.model.text.StructuredContent;
import de.digitalcollections.model.text.contentblock.Text;
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
    classes = {WebsiteRepositoryImpl.class})
@ContextConfiguration(classes = SpringConfigBackendTestDatabase.class)
@Sql(scripts = "classpath:cleanup_database.sql")
@DisplayName("The Website Repository")
class WebsiteRepositoryImplTest {

  WebsiteRepositoryImpl repo;

  @Autowired CudamiConfig cudamiConfig;

  @Autowired PostgreSQLContainer postgreSQLContainer;

  @Autowired WebpageRepositoryImpl webpageRepository;

  @Autowired Jdbi jdbi;

  @BeforeEach
  public void beforeEach() {
    repo = new WebsiteRepositoryImpl(jdbi, webpageRepository, cudamiConfig);
  }

  @Test
  @DisplayName("is testable")
  void containerIsUpAndRunning() {
    assertThat(postgreSQLContainer.isRunning()).isTrue();
  }

  @Test
  @DisplayName("can save and retrieve a website")
  void saveWebsite() {
    Website website =
        Website.builder()
            .label(Locale.GERMAN, "Digitale Sammlungen")
            .url("https://www.digitale-sammlungen.de")
            .registrationDate("2022-05-04")
            .rootPages(List.of(Webpage.builder().build()))
            .build();

    Website actual = repo.save(website);

    assertThat(actual.getEntityType()).isEqualTo(EntityType.WEBSITE);
    assertThat(actual.getUuid()).isNotNull();
  }

  @Test
  @DisplayName("save a website with notes")
  void saveWebsiteWithNotes() {
    StructuredContent noteContent1 = new StructuredContent();
    noteContent1.addContentBlock(new Text("eine Bemerkung"));
    LocalizedStructuredContent note1 = new LocalizedStructuredContent();
    note1.put(Locale.GERMAN, noteContent1);

    StructuredContent noteContent2 = new StructuredContent();
    noteContent2.addContentBlock(new Text("zweite Bemerkung"));
    LocalizedStructuredContent note2 = new LocalizedStructuredContent();
    note2.put(Locale.GERMAN, noteContent2);
    Website website =
        Website.builder()
            .label(Locale.GERMAN, "digiPress")
            .url("https://digipress.digitale-sammlungen.de")
            .registrationDate("2022-05-04")
            .rootPages(List.of(Webpage.builder().build()))
            .note(note1)
            .note(note2)
            .build();

    Website actual = repo.save(website);

    assertThat(actual.getNotes()).isEqualTo(website.getNotes());
    assertThat(actual.getUuid()).isNotNull();
  }
}
