package de.digitalcollections.cudami.server.controller.alias;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.alias.UrlAliasService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.cudami.server.model.LocalizedUrlAliasBuilder;
import de.digitalcollections.cudami.server.model.SearchPageResponseBuilder;
import de.digitalcollections.cudami.server.model.UrlAliasBuilder;
import de.digitalcollections.model.alias.LocalizedUrlAliases;
import de.digitalcollections.model.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.EntityType;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(UrlAliasController.class)
@DisplayName("The UrlAliasController")
class UrlAliasControllerTest extends BaseControllerTest {

  @MockBean private UrlAliasService urlAliasService;

  @DisplayName("returns a 404, when an UrlAlias could not be found")
  @ParameterizedTest
  @ValueSource(strings = {"/v5/urlaliases/12345678-1234-1234-1234-123456789012"})
  public void nonexistingUrlAlias(String path) throws Exception {
    when(urlAliasService.findOne(any(UUID.class))).thenReturn(null);

    testNotFound(path);
  }

  @DisplayName("returns an existingUrlAlias")
  @ParameterizedTest
  @ValueSource(strings = {"/v5/urlaliases/12345678-1234-1234-1234-123456789012"})
  public void existingUrlAlias(String path) throws Exception {
    UrlAlias expectedUrlAlias =
        new UrlAliasBuilder()
            .createdAt("2021-08-17T15:18:01.000001")
            .lastPublishedAt("2021-08-17T15:18:01.000001")
            .isMainAlias()
            .withSlug("hurz")
            .withTargetLanguage("de")
            .withTargetType(EntityType.COLLECTION)
            .withTargetUuid("23456789-2345-2345-2345-234567890123")
            .withUuid("12345678-1234-1234-1234-123456789012")
            .withWebsiteUuid("87654321-4321-4321-4321-876543210987")
            .build();
    when(urlAliasService.findOne(any(UUID.class))).thenReturn(expectedUrlAlias);

    testJson(path);
  }

  @DisplayName("returns a 404 on an attempt to delete an nonexisting UrlAlias")
  @ParameterizedTest
  @ValueSource(strings = {"/v5/urlaliases/12345678-1234-1234-1234-123456789012"})
  public void deleteNonexistingUrlAlias(String path) throws Exception {
    when(urlAliasService.delete(any(UUID.class))).thenReturn(false);

    testDeleteNotFound(path);
  }

  @DisplayName("returns a 204 after deleting an existing UrlAlias")
  @ParameterizedTest
  @ValueSource(strings = {"/v5/urlaliases/12345678-1234-1234-1234-123456789012"})
  public void deleteExistingUrlAlias(String path) throws Exception {
    when(urlAliasService.delete(any(UUID.class))).thenReturn(true);

    testDeleteSuccessful(path);
  }

  @DisplayName("successfully creates an UrlAlias")
  @ParameterizedTest
  @ValueSource(strings = {"/v5/urlaliases"})
  public void save(String path) throws Exception {
    UrlAlias expectedUrlAlias =
        new UrlAliasBuilder()
            .createdAt("2021-08-17T15:18:01.000001")
            .lastPublishedAt("2021-08-17T15:18:01.000001")
            .isMainAlias()
            .withSlug("hurz")
            .withTargetLanguage("de")
            .withTargetType(EntityType.COLLECTION)
            .withTargetUuid("23456789-2345-2345-2345-234567890123")
            .withUuid("12345678-1234-1234-1234-123456789012")
            .withWebsiteUuid("87654321-4321-4321-4321-876543210987")
            .build();
    when(urlAliasService.save(any(UrlAlias.class))).thenReturn(expectedUrlAlias);

    String body =
        "{\n"
            + "  \"created\": \"2021-08-17T15:18:01.000001\",\n"
            + "  \"lastPublished\": \"2021-08-17T15:18:01.000001\",\n"
            + "  \"mainAlias\": true,\n"
            + "  \"slug\": \"hurz\",\n"
            + "  \"targetLanguage\": \"de\",\n"
            + "  \"targetType\": \"COLLECTION\",\n"
            + "  \"targetUuid\": \"23456789-2345-2345-2345-234567890123\",\n"
            + "  \"websiteUuid\": \"87654321-4321-4321-4321-876543210987\"\n"
            + "}";

    testPostJson(path, body, "/v5/urlaliases/12345678-1234-1234-1234-123456789012.json");
  }

  @DisplayName("can return a find result with empty content")
  @ParameterizedTest
  @ValueSource(strings = {"/v5/urlaliases"})
  public void findWithEmptyResult(String path) throws Exception {
    SearchPageResponse<LocalizedUrlAliases> expected =
        (SearchPageResponse<LocalizedUrlAliases>)
            new SearchPageResponseBuilder().forPageSize(1).withTotalElements(0).build();

    when(urlAliasService.find(any(SearchPageRequest.class))).thenReturn(expected);

    testJson(path, "/v5/urlaliases/empty.json");
  }

  @DisplayName("can return a find result with existing content")
  @ParameterizedTest
  @ValueSource(strings = {"/v5/urlaliases?pageNumber=0&pageSize=1"})
  public void findWithFilledResult(String path) throws Exception {
    SearchPageResponse<LocalizedUrlAliases> expected =
        (SearchPageResponse<LocalizedUrlAliases>)
            new SearchPageResponseBuilder()
                .forPageSize(1)
                .withTotalElements(319)
                .withContent(
                    List.of(
                        new LocalizedUrlAliasBuilder()
                            .addUrlAlias(
                                new UrlAliasBuilder()
                                    .createdAt("2021-08-17T15:18:01.000001")
                                    .lastPublishedAt("2021-08-17T15:18:01.000001")
                                    .isMainAlias()
                                    .withSlug("hurz")
                                    .withTargetLanguage("de")
                                    .withTargetType(EntityType.COLLECTION)
                                    .withTargetUuid("23456789-2345-2345-2345-234567890123")
                                    .withUuid("12345678-1234-1234-1234-123456789012")
                                    .withWebsiteUuid("87654321-4321-4321-4321-876543210987")
                                    .build())
                            .build()))
                .build();

    when(urlAliasService.find(any(SearchPageRequest.class))).thenReturn(expected);

    testJson(path, "/v5/urlaliases/find_with_result.json");
  }
}
