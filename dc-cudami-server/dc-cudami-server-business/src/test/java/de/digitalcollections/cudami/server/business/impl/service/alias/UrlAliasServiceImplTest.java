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
    UrlAlias expected = createUrlAlias("hurz");

    when(repo.findOne(any(UUID.class))).thenReturn(expected);

    assertThat(service.findOne(UUID.randomUUID())).isEqualTo(expected);
  }

  // -------------------------------------------------------------------------
  private UrlAlias createUrlAlias(String slug) {
    UrlAlias urlAlias = new UrlAlias();
    urlAlias.setUuid(UUID.randomUUID());
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
}
