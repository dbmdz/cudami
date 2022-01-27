package de.digitalcollections.cudami.server.backend.impl.jdbi.legal;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.server.backend.impl.database.config.SpringConfigBackendDatabase;
import de.digitalcollections.cudami.server.backend.impl.model.TestModelFixture;
import de.digitalcollections.model.legal.License;
import java.util.Locale;
import java.util.Map;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    classes = {LicenseRepositoryImpl.class})
@ContextConfiguration(classes = SpringConfigBackendDatabase.class)
@DisplayName("Test for License Repository")
public class LicenseRepositoryImplTest {

  @Autowired Jdbi jdbi;
  @Autowired PostgreSQLContainer postgreSQLContainer;
  LicenseRepositoryImpl repo;

  @BeforeEach
  public void beforeEach() {
    repo = new LicenseRepositoryImpl(jdbi);
  }

  @Test
  @DisplayName("is testable")
  void containerIsUpAndRunning() {
    assertThat(postgreSQLContainer.isRunning()).isTrue();
  }

  private License createLicense() {
    License license =
        TestModelFixture.createLicense(
            "NoC-NC 1.0",
            Map.of(
                Locale.GERMAN, "Kein Urheberrechtsschutz – nur nicht-kommerzielle Nutzung erlaubt",
                Locale.ENGLISH, "No Copyright – Non-Commercial Use Only"),
            "http://rightsstatements.org/vocab/NoC-NC/1.0/");
    return license;
  }

  @Test
  @DisplayName("should save a license")
  public void testSave() {
    License license = createLicense();

    License actual = repo.save(license);

    assertThat(actual.getAcronym()).isEqualTo("NoC-NC 1.0");
    assertThat(actual.getLabel().getText(Locale.GERMAN))
        .isEqualTo("Kein Urheberrechtsschutz – nur nicht-kommerzielle Nutzung erlaubt");
    assertThat(actual.getLabel().getText(Locale.ENGLISH))
        .isEqualTo("No Copyright – Non-Commercial Use Only");
    assertThat(actual.getUrl().toString())
        .isEqualTo("http://rightsstatements.org/vocab/NoC-NC/1.0/");

    repo.delete(actual.getUuid());
  }

  @Test
  @Rollback(true)
  public void testUpdate() {
    License license = createLicense();

    License actual = repo.save(license);

    actual.setAcronym("UPDATED");
    License updatedLicense = repo.update(actual);

    assertThat(updatedLicense.getAcronym()).isEqualTo("UPDATED");
    assertThat(updatedLicense.getLabel().getText(Locale.GERMAN))
        .isEqualTo("Kein Urheberrechtsschutz – nur nicht-kommerzielle Nutzung erlaubt");
    assertThat(updatedLicense.getLabel().getText(Locale.ENGLISH))
        .isEqualTo("No Copyright – Non-Commercial Use Only");
    assertThat(updatedLicense.getUrl().toString())
        .isEqualTo("http://rightsstatements.org/vocab/NoC-NC/1.0/");

    repo.delete(updatedLicense.getUuid());
  }
}
