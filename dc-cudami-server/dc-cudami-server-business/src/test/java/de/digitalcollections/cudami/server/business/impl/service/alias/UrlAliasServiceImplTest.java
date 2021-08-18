package de.digitalcollections.cudami.server.business.impl.service.alias;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.backend.api.repository.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.CudamiServiceException;
import de.digitalcollections.model.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.EntityType;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The UrlAliasService implementation")
class UrlAliasServiceImplTest {

  private UrlAliasServiceImpl service;

  private UrlAliasRepository repo;

  @BeforeEach
  public void beforeEach() {
    repo = mock(UrlAliasRepository.class);
    service = new UrlAliasServiceImpl(repo);
  }

  @DisplayName("returns null, when an nonexisting UrlAlias should be retrieved")
  @Test
  public void readNonexisting() throws CudamiServiceException {
    when(repo.findOne(any(UUID.class))).thenReturn(null);

    assertThat(service.findOne(UUID.randomUUID())).isNull();
  }

  @DisplayName("returns null, an UrlAlias with uuid=null should be retrieved")
  @Test
  public void readNull() throws CudamiServiceException {
    when(repo.findOne(eq(null))).thenReturn(null);

    assertThat(service.findOne(null)).isNull();
  }

  @DisplayName("raises a ServiceException when the repository throws an exception")
  @Test
  public void raiseException() throws CudamiServiceException {
    when(repo.findOne(any(UUID.class))).thenThrow(new NullPointerException("foo"));

    assertThrows(
        CudamiServiceException.class,
        () -> {
          service.findOne(UUID.randomUUID());
        });
  }

  @DisplayName("returns an UrlAlias")
  @Test
  public void readExisting() throws CudamiServiceException {
    UrlAlias expected = createUrlAlias("hützligrütz", false);

    when(repo.findOne(any(UUID.class))).thenReturn(expected);

    assertThat(service.findOne(UUID.randomUUID())).isEqualTo(expected);
  }

  @DisplayName("raises a ServiceException when trying to save an UrlAlias with existing UUID")
  @Test
  public void raiseExceptionWhenSaveWithUuid() throws CudamiServiceException {
    assertThrows(
        CudamiServiceException.class,
        () -> {
          service.save(createUrlAlias("hützligrütz", true));
        });
  }

  @DisplayName("raises a ServiceException when saving leads to an exception in the repository")
  @Test
  public void raiseExceptionWhenSaveLeadsToAnException() throws CudamiServiceException {
    when(repo.save(any(UrlAlias.class))).thenThrow(new NullPointerException("foo"));

    assertThrows(
        CudamiServiceException.class,
        () -> {
          service.save(createUrlAlias("hützligrütz", false));
        });
  }

  @DisplayName("saves an UrlAlias and returns it with set UUID")
  @Test
  public void saveUrlAlias() throws CudamiServiceException {
    UrlAlias urlAlias = createUrlAlias("hützligrütz", false);
    UrlAlias expected = deepCopy(urlAlias);
    expected.setUuid(UUID.randomUUID());

    when(repo.save(eq(urlAlias))).thenReturn(expected);

    assertThat(service.save(urlAlias)).isEqualTo(expected);
  }

  // -------------------------------------------------------------------------
  private UrlAlias createUrlAlias(String slug, boolean setUuid) {
    UrlAlias urlAlias = new UrlAlias();
    if (setUuid) {
      urlAlias.setUuid(UUID.randomUUID());
    }
    urlAlias.setMainAlias(false);
    urlAlias.setTargetUuid(UUID.randomUUID());
    urlAlias.setSlug(slug);
    urlAlias.setTargetType(EntityType.COLLECTION);
    urlAlias.setLastPublished(LocalDateTime.now());
    urlAlias.setCreated(LocalDateTime.now());
    urlAlias.setTargetLanguage(Locale.forLanguageTag("de"));
    urlAlias.setWebsiteUuid(UUID.randomUUID());
    return urlAlias;
  }

  private UrlAlias deepCopy(UrlAlias urlAlias) {
    UrlAlias copy = new UrlAlias();
    copy.setMainAlias(urlAlias.isMainAlias());
    copy.setTargetLanguage(urlAlias.getTargetLanguage());
    copy.setUuid(urlAlias.getUuid());
    copy.setCreated(urlAlias.getCreated());
    copy.setWebsiteUuid(urlAlias.getWebsiteUuid());
    copy.setLastPublished(urlAlias.getLastPublished());
    copy.setSlug(urlAlias.getSlug());
    copy.setTargetType(urlAlias.getTargetType());
    return copy;
  }
}
