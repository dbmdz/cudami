package de.digitalcollections.cudami.server.backend.impl.jdbi.legal;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.impl.database.config.SpringConfigBackendDatabase;
import de.digitalcollections.cudami.server.backend.impl.model.TestModelFixture;
import de.digitalcollections.model.legal.License;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    classes = {LicenseRepositoryImpl.class})
@ContextConfiguration(classes = SpringConfigBackendDatabase.class)
@DisplayName("Test for License Repository")
@Sql(scripts = "classpath:cleanup_database.sql")
public class LicenseRepositoryImplTest {

  @Autowired Jdbi jdbi;
  @Autowired PostgreSQLContainer postgreSQLContainer;
  @Autowired CudamiConfig cudamiConfig;
  LicenseRepositoryImpl repo;

  @BeforeEach
  public void beforeEach() {
    repo = new LicenseRepositoryImpl(jdbi, cudamiConfig);
  }

  @Test
  @DisplayName("is testable")
  void containerIsUpAndRunning() {
    assertThat(postgreSQLContainer.isRunning()).isTrue();
  }

  private License createLicense() {
    return createLicense("");
  }

  private License createLicense(String postfix) {
    License license =
        TestModelFixture.createLicense(
            "NoC-NC 1.0" + postfix,
            Map.of(
                Locale.GERMAN,
                "Kein Urheberrechtsschutz – nur nicht-kommerzielle Nutzung erlaubt" + postfix,
                Locale.ENGLISH,
                "No Copyright – Non-Commercial Use Only" + postfix),
            "http://rightsstatements.org/vocab/NoC-NC/1.0/" + postfix);
    return license;
  }

  @Test
  @DisplayName("should return count of records in table")
  public void testCount() {
    License license1 = createLicense("1");
    License actual = repo.save(license1);
    UUID uuid1 = actual.getUuid();

    License license2 = createLicense("2");
    actual = repo.save(license2);
    UUID uuid2 = actual.getUuid();

    long count = repo.count();
    assertThat(count).isEqualTo(2);

    repo.deleteByUuids(List.of(uuid1, uuid2));
  }

  @Test
  @DisplayName("should delete a license by given url")
  public void testDeleteByUrl() {
    License license = createLicense();
    License actual = repo.save(license);
    UUID uuid = actual.getUuid();

    repo.deleteByUrl(actual.getUrl());

    License result = repo.getByUuid(uuid);

    assertThat(result).isNull();
  }

  @Test
  @DisplayName("should delete a license by given uuid")
  public void testDeleteByUuid() {
    License license = createLicense();
    License actual = repo.save(license);
    UUID uuid = actual.getUuid();

    repo.deleteByUuid(uuid);

    License result = repo.getByUuid(uuid);

    assertThat(result).isNull();
  }

  @Test
  @DisplayName("should delete a list of licenses by given uuids")
  public void testDeleteByUuids() {
    License license1 = createLicense("1");
    License actual = repo.save(license1);
    UUID uuid1 = actual.getUuid();

    License license2 = createLicense("2");
    actual = repo.save(license2);
    UUID uuid2 = actual.getUuid();

    repo.deleteByUuids(List.of(uuid1, uuid2));

    license1 = repo.getByUuid(uuid1);
    assertThat(license1).isNull();

    license2 = repo.getByUuid(uuid2);
    assertThat(license2).isNull();
  }

  @Test
  @DisplayName("should get a paged list of licenses")
  public void testFind() {
    // create 4 licenses
    License license1 = createLicense("1");
    license1 = repo.save(license1);

    License license2 = createLicense("2");
    license2 = repo.save(license2);

    License license3 = createLicense("3");
    license3 = repo.save(license3);

    License license4 = createLicense("4");
    license4 = repo.save(license4);

    // do find
    PageRequest pageRequest = PageRequest.defaultBuilder().pageSize(2).pageNumber(0).build();
    PageResponse<License> pageResponse = repo.find(pageRequest);
    assertThat(pageResponse.getTotalPages()).isEqualTo(2);
    assertThat(pageResponse.getTotalElements()).isEqualTo(4);

    // do cleanup
    repo.deleteByUuid(license1.getUuid());
    repo.deleteByUuid(license2.getUuid());
    repo.deleteByUuid(license3.getUuid());
    repo.deleteByUuid(license4.getUuid());
  }

  @Test
  @DisplayName("should get the complete list of licenses")
  public void testFindAll() {
    // create 4 licenses
    License license1 = createLicense("1");
    license1 = repo.save(license1);

    License license2 = createLicense("2");
    license2 = repo.save(license2);

    License license3 = createLicense("3");
    license3 = repo.save(license3);

    License license4 = createLicense("4");
    license4 = repo.save(license4);

    // do find
    List<License> list = repo.findAll();
    assertThat(list.size()).isEqualTo(4);

    // do cleanup
    repo.deleteByUuid(license1.getUuid());
    repo.deleteByUuid(license2.getUuid());
    repo.deleteByUuid(license3.getUuid());
    repo.deleteByUuid(license4.getUuid());
  }

  @Test
  @DisplayName("should get a license by given url")
  public void testGetByUrl() {
    License license = createLicense();
    License actual = repo.save(license);
    UUID uuid = actual.getUuid();

    final URL url = actual.getUrl();
    License result = repo.getByUrl(url);
    assertThat(result).isNotNull();

    repo.deleteByUuid(uuid);
  }

  @Test
  @DisplayName("should get a license by given uuid")
  public void testGetByUuid() {
    License license = createLicense();
    License actual = repo.save(license);
    UUID uuid = actual.getUuid();

    License result = repo.getByUuid(uuid);
    assertThat(result).isNotNull();

    repo.deleteByUuid(uuid);
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

    repo.deleteByUuid(actual.getUuid());
  }

  @Test
  @DisplayName("should update a license")
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

    repo.deleteByUuid(updatedLicense.getUuid());
  }
}
