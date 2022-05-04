package de.digitalcollections.cudami.server.controller.identifiable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.IdentifiableType;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.EntityType;
import de.digitalcollections.model.identifiable.entity.Website;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(IdentifiableController.class)
@DisplayName("The IdentifiableController")
public class IdentifiableControllerTest extends BaseControllerTest {

  @MockBean(name = "identifiableService")
  private IdentifiableService identifiableService;

  @MockBean private UrlAliasService urlAliasService;

  @DisplayName("can return an empty LocalizedUrlAlias")
  @ParameterizedTest
  @ValueSource(
      strings = {"/v5/identifiables/12345678-1234-1234-1234-123456789012/localizedUrlAliases"})
  public void emptyLocalizedUrlAlias(String path) throws Exception {
    Identifiable dummyIdentifiable = mock(Identifiable.class);
    when(identifiableService.getByUuid(any(UUID.class))).thenReturn(dummyIdentifiable);
    LocalizedUrlAliases expected = new LocalizedUrlAliases();
    when(urlAliasService.getLocalizedUrlAliases(any(UUID.class))).thenReturn(expected);

    testJson(path, "/v5/identifiables/localizedUrlAliases_empty.json");
  }

  @DisplayName("can return a LocalizedUrlAlias")
  @ParameterizedTest
  @ValueSource(
      strings = {"/v5/identifiables/12345678-1234-1234-1234-123456789012/localizedUrlAliases"})
  public void localizedUrlAlias(String path) throws Exception {
    Identifiable dummyIdentifiable = mock(Identifiable.class);
    when(identifiableService.getByUuid(any(UUID.class))).thenReturn(dummyIdentifiable);
    LocalizedUrlAliases expected = new LocalizedUrlAliases();
    UrlAlias urlAlias1 =
        UrlAlias.builder()
            .created("2021-08-17T15:18:01.000001")
            .lastPublished("2021-08-17T15:18:01.000001")
            .isPrimary()
            .slug("hurz")
            .targetLanguage("de")
            .targetType(IdentifiableType.ENTITY, EntityType.COLLECTION)
            .targetUuid("23456789-2345-2345-2345-234567890123")
            .uuid("12345678-1234-1234-1234-123456789012")
            .website(Website.builder().uuid("87654321-4321-4321-4321-876543210987").build())
            .build();
    UrlAlias urlAlias2 =
        UrlAlias.builder()
            .created("2021-08-17T15:18:01.000002")
            .lastPublished("2021-08-17T15:18:01.000002")
            .isPrimary()
            .slug("hützligrütz")
            .targetLanguage("de")
            .targetType(IdentifiableType.ENTITY, EntityType.DIGITAL_OBJECT)
            .targetUuid("23456789-2345-2345-2345-234567890124")
            .uuid("12345678-1234-1234-1234-123456789012")
            .website(Website.builder().uuid("87654321-4321-4321-4321-876543210987").build())
            .build();
    expected.add(urlAlias1, urlAlias2);
    when(urlAliasService.getLocalizedUrlAliases(any(UUID.class))).thenReturn(expected);

    testJson(path, "/v5/identifiables/localizedUrlAliases.json");
  }

  @DisplayName("returns a 404, when an identifiable could not be found")
  @ParameterizedTest
  @ValueSource(
      strings = {"/v5/identifiables/12345678-1234-1234-1234-123456789012/localizedUrlAliases"})
  public void nonexistingUrlAlias(String path) throws Exception {
    when(identifiableService.getByUuid(any(UUID.class))).thenReturn(null);

    testNotFound(path);
  }
}
