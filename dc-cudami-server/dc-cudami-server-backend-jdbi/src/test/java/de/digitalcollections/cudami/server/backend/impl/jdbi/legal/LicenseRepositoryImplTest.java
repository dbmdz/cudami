package de.digitalcollections.cudami.server.backend.impl.jdbi.legal;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.impl.database.config.SpringConfigBackendTestDatabase;
import de.digitalcollections.cudami.server.backend.impl.model.TestModelFixture;
import de.digitalcollections.model.legal.License;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.validation.ValidationException;
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

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    classes = {LicenseRepositoryImpl.class})
@ContextConfiguration(classes = SpringConfigBackendTestDatabase.class)
@DisplayName("Test for License Repository")
@Sql(scripts = "classpath:cleanup_database.sql")
public class LicenseRepositoryImplTest {

  @Autowired Jdbi jdbi;
  @Autowired CudamiConfig cudamiConfig;
  LicenseRepositoryImpl repo;

  @BeforeEach
  public void beforeEach() {
    repo = new LicenseRepositoryImpl(jdbi, cudamiConfig);
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
  public void testCount() throws RepositoryException, ValidationException {
    License license1 = createLicense("1");
    repo.save(license1);
    UUID uuid1 = license1.getUuid();

    License license2 = createLicense("2");
    repo.save(license2);
    UUID uuid2 = license2.getUuid();

    long count = repo.count();
    assertThat(count).isEqualTo(2);

    repo.deleteByUuids(List.of(uuid1, uuid2));
  }

  @Test
  @DisplayName("should delete a license by given url")
  public void testDeleteByUrl() throws RepositoryException, ValidationException {
    License license = createLicense();
    repo.save(license);
    UUID uuid = license.getUuid();

    repo.deleteByUrl(license.getUrl());

    License result = repo.getByUuid(uuid);

    assertThat(result).isNull();
  }

  @Test
  @DisplayName("should delete a license by given uuid")
  public void testDeleteByUuid() throws RepositoryException, ValidationException {
    License license = createLicense();
    repo.save(license);
    UUID uuid = license.getUuid();

    repo.deleteByUuid(uuid);

    License result = repo.getByUuid(uuid);

    assertThat(result).isNull();
  }

  @Test
  @DisplayName("should delete a list of licenses by given uuids")
  public void testDeleteByUuids() throws RepositoryException, ValidationException {
    License license1 = createLicense("1");
    repo.save(license1);
    UUID uuid1 = license1.getUuid();

    License license2 = createLicense("2");
    repo.save(license2);
    UUID uuid2 = license2.getUuid();

    repo.deleteByUuids(List.of(uuid1, uuid2));

    license1 = repo.getByUuid(uuid1);
    assertThat(license1).isNull();

    license2 = repo.getByUuid(uuid2);
    assertThat(license2).isNull();
  }

  @Test
  @DisplayName("should get a paged list of licenses")
  public void testFind() throws RepositoryException, ValidationException {
    // create 4 licenses
    License license1 = createLicense("1");
    repo.save(license1);

    License license2 = createLicense("2");
    repo.save(license2);

    License license3 = createLicense("3");
    repo.save(license3);

    License license4 = createLicense("4");
    repo.save(license4);

    // do find
    PageRequest pageRequest = PageRequest.builder().pageSize(2).pageNumber(0).build();
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
  @DisplayName("should get a license by given url")
  public void testGetByUrl() throws RepositoryException, ValidationException {
    License license = createLicense();
    repo.save(license);
    UUID uuid = license.getUuid();

    final URL url = license.getUrl();
    License result = repo.getByUrl(url);
    assertThat(result).isNotNull();

    repo.deleteByUuid(uuid);
  }

  @Test
  @DisplayName("should get a license by given uuid")
  public void testGetByUuid() throws RepositoryException, ValidationException {
    License license = createLicense();
    repo.save(license);
    UUID uuid = license.getUuid();

    License result = repo.getByUuid(uuid);
    assertThat(result).isNotNull();

    repo.deleteByUuid(uuid);
  }

  @Test
  @DisplayName("should save a license")
  public void testSave() throws RepositoryException, ValidationException {
    License license = createLicense();
    repo.save(license);

    assertThat(license.getAcronym()).isEqualTo("NoC-NC 1.0");
    assertThat(license.getLabel().getText(Locale.GERMAN))
        .isEqualTo("Kein Urheberrechtsschutz – nur nicht-kommerzielle Nutzung erlaubt");
    assertThat(license.getLabel().getText(Locale.ENGLISH))
        .isEqualTo("No Copyright – Non-Commercial Use Only");
    assertThat(license.getUrl().toString())
        .isEqualTo("http://rightsstatements.org/vocab/NoC-NC/1.0/");

    repo.deleteByUuid(license.getUuid());
  }

  @Test
  @DisplayName("should update a license")
  public void testUpdate() throws RepositoryException, ValidationException {
    License license = createLicense();
    repo.save(license);

    license.setAcronym("UPDATED");
    repo.update(license);

    License updatedLicense = repo.getByUuid(license.getUuid());

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
