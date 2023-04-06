package de.digitalcollections.cudami.server.controller.identifiable.alias;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(V5UrlAliasController.class)
@DisplayName("The V5UrlAliasController")
class V5UrlAliasControllerTest extends BaseControllerTest {

  @MockBean private UrlAliasService urlAliasService;

  @DisplayName("returns an error state when creating an UrlAlias with already set uuid")
  @Test
  public void createWithSetUuidLeadsToError() throws Exception {
    String body =
        "{\n"
            + "  \"created\": \"2021-08-17T15:18:01.000001\",\n"
            + "  \"lastPublished\": \"2021-08-17T15:18:01.000001\",\n"
            + "  \"primary\": true,\n"
            + "  \"slug\": \"hurz\",\n"
            + "  \"targetLanguage\": \"de\",\n"
            + "  \"targetIdentifiableType\": \"ENTITY\",\n"
            + "  \"targetIdentifiableObjectType\": \"COLLECTION\",\n"
            + "  \"targetEntityType\": \"COLLECTION\",\n"
            + "  \"uuid\": \"12345678-1234-1234-1234-123456789012\",\n"
            + "  \"targetUuid\": \"23456789-2345-2345-2345-234567890123\",\n"
            + "  \"websiteUuid\": \"87654321-4321-4321-4321-876543210987\",\n"
            + "  \"objectType\": \"URL_ALIAS\"\n"
            + "}";
    testPostJsonWithState("/v5/urlaliases", body, 422);
  }

  @DisplayName("can return a find result with empty content")
  @ParameterizedTest
  @ValueSource(strings = {"/v5/urlaliases"})
  public void findWithEmptyResult(String path) throws Exception {
    PageResponse<LocalizedUrlAliases> expected =
        PageResponse.builder().forPageSize(1).withTotalElements(0).build();

    when(urlAliasService.findLocalizedUrlAliases(any(PageRequest.class))).thenReturn(expected);

    testJson(path, "/v5/urlaliases/empty.json");
  }

  @DisplayName("can return a find result with existing content")
  @ParameterizedTest
  @ValueSource(strings = {"/v5/urlaliases?pageNumber=0&pageSize=1"})
  public void findWithFilledResult(String path) throws Exception {
    PageResponse<LocalizedUrlAliases> expected =
        PageResponse.builder()
            .forPageSize(1)
            .withTotalElements(319)
            .withContent(
                List.of(
                    new LocalizedUrlAliases(
                        UrlAlias.builder()
                            .created("2021-08-17T15:18:01.000001")
                            .lastPublished("2021-08-17T15:18:01.000001")
                            .isPrimary()
                            .slug("hurz")
                            .targetLanguage("de")
                            .target(
                                Collection.builder()
                                    .uuid("23456789-2345-2345-2345-234567890123")
                                    .build())
                            .uuid("12345678-1234-1234-1234-123456789012")
                            .website(
                                Website.builder()
                                    .uuid("87654321-4321-4321-4321-876543210987")
                                    .build())
                            .build())))
            .build();

    when(urlAliasService.findLocalizedUrlAliases(any(PageRequest.class))).thenReturn(expected);

    testJson(path, "/v5/urlaliases/find_with_result.json");
  }

  @DisplayName("successfully creates an UrlAlias")
  @ParameterizedTest
  @ValueSource(strings = {"/v5/urlaliases"})
  public void save(String path) throws Exception {
    UrlAlias expectedUrlAlias =
        UrlAlias.builder()
            .created("2021-08-17T15:18:01.000001")
            .lastPublished("2021-08-17T15:18:01.000001")
            .isPrimary()
            .slug("hurz")
            .targetLanguage("de")
            .target(Collection.builder().uuid("23456789-2345-2345-2345-234567890123").build())
            .uuid("12345678-1234-1234-1234-123456789012")
            .website(Website.builder().uuid("87654321-4321-4321-4321-876543210987").build())
            .build();

    doAnswer(invocation -> replaceFirstArgumentData(expectedUrlAlias, invocation))
        .when(urlAliasService)
        .save(any(UrlAlias.class));

    String body =
        "{\n"
            + "  \"created\": \"2021-08-17T15:18:01.000001\",\n"
            + "  \"lastPublished\": \"2021-08-17T15:18:01.000001\",\n"
            + "  \"primary\": true,\n"
            + "  \"slug\": \"hurz\",\n"
            + "  \"targetLanguage\": \"de\",\n"
            + "  \"targetIdentifiableType\": \"ENTITY\",\n"
            + "  \"targetIdentifiableObjectType\": \"COLLECTION\",\n"
            + "  \"targetEntityType\": \"COLLECTION\",\n"
            + "  \"targetUuid\": \"23456789-2345-2345-2345-234567890123\",\n"
            + "  \"websiteUuid\": \"87654321-4321-4321-4321-876543210987\",\n"
            + "  \"objectType\": \"URL_ALIAS\"\n"
            + "}";
    testPostJson(path, body, "/v5/urlaliases/12345678-1234-1234-1234-123456789012.json");
  }

  @DisplayName("successfully updates an UrlAlias")
  @ParameterizedTest
  @ValueSource(strings = {"/v5/urlaliases/12345678-1234-1234-1234-123456789012"})
  public void update(String path) throws Exception {
    UrlAlias expectedUrlAlias =
        UrlAlias.builder()
            .created("2021-08-17T15:18:01.000001")
            .lastPublished("2021-08-17T15:18:01.000001")
            .isPrimary()
            .slug("hurz")
            .targetLanguage("de")
            .target(Collection.builder().uuid("23456789-2345-2345-2345-234567890123").build())
            .uuid("12345678-1234-1234-1234-123456789012")
            .website(Website.builder().uuid("87654321-4321-4321-4321-876543210987").build())
            .build();

    doAnswer(invocation -> replaceFirstArgumentData(expectedUrlAlias, invocation))
        .when(urlAliasService)
        .update(any(UrlAlias.class));

    String body =
        "{\n"
            + "  \"created\": \"2021-08-17T15:18:01.000001\",\n"
            + "  \"lastPublished\": \"2021-08-17T15:18:01.000001\",\n"
            + "  \"primary\": true,\n"
            + "  \"slug\": \"hurz\",\n"
            + "  \"targetLanguage\": \"de\",\n"
            + "  \"targetIdentifiableType\": \"ENTITY\",\n"
            + "  \"targetIdentifiableObjectType\": \"COLLECTION\",\n"
            + "  \"targetEntityType\": \"COLLECTION\",\n"
            + "  \"uuid\": \"12345678-1234-1234-1234-123456789012\",\n"
            + "  \"targetUuid\": \"23456789-2345-2345-2345-234567890123\",\n"
            + "  \"website\": {\n"
            + "     \"identifiers\":[],\n"
            + "     \"type\":\"ENTITY\",\n"
            + "     \"uuid\":\"87654321-4321-4321-4321-876543210987\",\n"
            + "     \"entityType\":\"WEBSITE\",\n"
            + "     \"identifiableObjectType\":\"WEBSITE\",\n"
            + "     \"refId\":0\n"
            + "  },\n"
            + "  \"objectType\": \"URL_ALIAS\"\n"
            + "}";
    testPutJson(path, body, "/v5/urlaliases/12345678-1234-1234-1234-123456789012.json");
  }

  private static Object replaceFirstArgumentData(UrlAlias expected, InvocationOnMock invocation) {
    Object[] args = invocation.getArguments();
    ((UrlAlias) args[0]).setUuid(expected.getUuid());
    ((UrlAlias) args[0]).setPrimary(expected.isPrimary());
    ((UrlAlias) args[0]).setWebsite(expected.getWebsite());
    return null;
  }
}
