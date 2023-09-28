package de.digitalcollections.cudami.server.controller.identifiable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.IdentifiableObjectType;
import de.digitalcollections.model.identifiable.IdentifiableType;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

  @DisplayName("can return a single identifiable")
  @ParameterizedTest
  @ValueSource(strings = {"/v6/identifiables/12345678-1234-1234-1234-123456789012"})
  public void getIdentifiableByUuid(String path) throws Exception {
    Identifiable example = Identifiable.class.getDeclaredConstructor().newInstance();
    example.setUuid(extractFirstUuidFromPath(path));

    Identifiable identifiable = Identifiable.builder().uuid(example.getUuid()).build();

    when(identifiableService.getByExamples(eq(List.of(example)))).thenReturn(List.of(identifiable));
    testJson(path, "/v6/identifiables/12345678-1234-1234-1234-123456789012.json");
  }

  @DisplayName("can return multiple identifiables with a GET request")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v6/identifiables/list/12345678-1234-1234-1234-123456789012,22345678-1234-1234-1234-123456789012"
      })
  public void getMultipleIdentifiablesByUuid(String path) throws Exception {
    Identifiable example1 = Identifiable.class.getDeclaredConstructor().newInstance();
    example1.setUuid(extractNthUuidFromPath(path, 0));

    Identifiable example2 = Identifiable.class.getDeclaredConstructor().newInstance();
    example2.setUuid(extractNthUuidFromPath(path, 1));

    Identifiable identifiable1 = Identifiable.builder().uuid(example1.getUuid()).build();
    Identifiable identifiable2 = Identifiable.builder().uuid(example2.getUuid()).build();

    when(identifiableService.getByExamples(eq(List.of(example1, example2))))
        .thenReturn(List.of(identifiable1, identifiable2));
    testJson(path, "/v6/identifiables/list.json");
  }

  @DisplayName("can return multiple identifiables with a POST request")
  @Test
  public void getByManyUuids() throws Exception {
    List<UUID> uuidList =
        List.of(
            UUID.fromString("12345678-1234-1234-1234-123456789012"),
            UUID.fromString("22345678-1234-1234-1234-123456789012"));
    String jsonBody = new DigitalCollectionsObjectMapper().writeValueAsString(uuidList);

    Identifiable example1 = Identifiable.class.getDeclaredConstructor().newInstance();
    example1.setUuid(UUID.fromString("12345678-1234-1234-1234-123456789012"));

    Identifiable example2 = Identifiable.class.getDeclaredConstructor().newInstance();
    example2.setUuid(UUID.fromString("22345678-1234-1234-1234-123456789012"));

    Identifiable identifiable1 = Identifiable.builder().uuid(example1.getUuid()).build();
    Identifiable identifiable2 = Identifiable.builder().uuid(example2.getUuid()).build();

    when(identifiableService.getByExamples(eq(List.of(example1, example2))))
        .thenReturn(List.of(identifiable1, identifiable2));

    testPostJson("/v6/identifiables/list", jsonBody, "/v6/identifiables/list.json");
  }

  @DisplayName("can return an empty LocalizedUrlAlias")
  @ParameterizedTest
  @ValueSource(
      strings = {"/v5/identifiables/12345678-1234-1234-1234-123456789012/localizedUrlAliases"})
  public void emptyLocalizedUrlAlias(String path) throws Exception {
    Identifiable dummyIdentifiable = mock(Identifiable.class);
    when(identifiableService.getByExample(any(Identifiable.class))).thenReturn(dummyIdentifiable);
    LocalizedUrlAliases expected = new LocalizedUrlAliases();
    when(urlAliasService.getByIdentifiable(any(Identifiable.class))).thenReturn(expected);

    testJson(path, "/v5/identifiables/localizedUrlAliases_empty.json");
  }

  @DisplayName("can return a LocalizedUrlAlias")
  @ParameterizedTest
  @ValueSource(
      strings = {"/v5/identifiables/12345678-1234-1234-1234-123456789012/localizedUrlAliases"})
  public void localizedUrlAlias(String path) throws Exception {
    Identifiable dummyIdentifiable = mock(Identifiable.class);
    when(identifiableService.getByExample(any(Identifiable.class))).thenReturn(dummyIdentifiable);
    LocalizedUrlAliases expected = new LocalizedUrlAliases();
    UrlAlias urlAlias1 =
        UrlAlias.builder()
            .created("2021-08-17T15:18:01.000001")
            .lastPublished("2021-08-17T15:18:01.000001")
            .isPrimary()
            .slug("hurz")
            .targetLanguage("de")
            .target(
                Collection.builder()
                    .type(IdentifiableType.ENTITY)
                    .identifiableObjectType(IdentifiableObjectType.COLLECTION)
                    .uuid("23456789-2345-2345-2345-234567890123")
                    .build())
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
            .target(
                DigitalObject.builder()
                    .type(IdentifiableType.ENTITY)
                    .identifiableObjectType(IdentifiableObjectType.DIGITAL_OBJECT)
                    .uuid("23456789-2345-2345-2345-234567890124")
                    .build())
            .uuid("12345678-1234-1234-1234-123456789012")
            .website(Website.builder().uuid("87654321-4321-4321-4321-876543210987").build())
            .build();
    expected.add(urlAlias1, urlAlias2);
    when(urlAliasService.getByIdentifiable(any(Identifiable.class))).thenReturn(expected);

    testJson(path, "/v5/identifiables/localizedUrlAliases.json");
  }

  @DisplayName("returns a 404, when an identifiable could not be found")
  @ParameterizedTest
  @ValueSource(
      strings = {"/v5/identifiables/12345678-1234-1234-1234-123456789012/localizedUrlAliases"})
  public void nonexistingUrlAlias(String path) throws Exception {
    when(identifiableService.getByExample(any(Identifiable.class))).thenReturn(null);

    testNotFound(path);
  }

  @DisplayName("can retrieve by identifier with plaintext id")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v6/identifiables/identifier/foo:bar",
        "/v5/identifiables/identifier/foo:bar",
        "/v2/identifiables/identifier/foo:bar",
        "/latest/identifiables/identifier/foo:bar",
        "/v6/identifiables/identifier/foo:bar.json",
        "/v5/identifiables/identifier/foo:bar.json",
        "/v2/identifiables/identifier/foo:bar.json",
        "/latest/identifiables/identifier/foo:bar.json"
      })
  void testGetByIdentifierWithPlaintextId(String path) throws Exception {
    Identifiable expected = Identifiable.builder().build();

    when(identifiableService.getByIdentifier(
            Identifier.builder().namespace("foo").id("bar").build()))
        .thenReturn(expected);

    testHttpGet(path);

    verify(identifiableService, times(1))
        .getByIdentifier(eq(Identifier.builder().namespace("foo").id("bar").build()));
  }

  @DisplayName("can retrieve by identifier with base 64 encoded data")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v6/identifiables/identifier/",
        "/v5/identifiables/identifier/",
        "/v2/identifiables/identifier/",
        "/latest/identifiables/identifier/"
      })
  void testGetByIdentifierWithBase64EncodedData(String basePath) throws Exception {
    Identifiable expected = Identifiable.builder().build();

    when(identifiableService.getByIdentifier(
            eq(Identifier.builder().namespace("foo").id("bar/bla").build())))
        .thenReturn(expected);

    testHttpGet(
        basePath
            + Base64.getEncoder().encodeToString("foo:bar/bla".getBytes(StandardCharsets.UTF_8)));

    verify(identifiableService, times(1))
        .getByIdentifier(eq(Identifier.builder().namespace("foo").id("bar/bla").build()));
  }
}
