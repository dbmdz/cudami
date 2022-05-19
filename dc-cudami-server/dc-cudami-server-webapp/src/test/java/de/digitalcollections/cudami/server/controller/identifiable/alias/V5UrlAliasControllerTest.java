package de.digitalcollections.cudami.server.controller.identifiable.alias;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.IdentifiableType;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.EntityType;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(V5UrlAliasController.class)
@DisplayName("The V5UrlAliasController")
class V5UrlAliasControllerTest extends BaseControllerTest {

  @MockBean private UrlAliasService urlAliasService;

  @DisplayName("can return a find result with empty content")
  @ParameterizedTest
  @ValueSource(strings = {"/v5/urlaliases"})
  public void findWithEmptyResult(String path) throws Exception {
    PageResponse<LocalizedUrlAliases> expected =
        PageResponse.builder().forPageSize(1).withTotalElements(0).build();

    when(urlAliasService.find(any(PageRequest.class))).thenReturn(expected);

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
                            .targetType(IdentifiableType.ENTITY, EntityType.COLLECTION)
                            .targetUuid("23456789-2345-2345-2345-234567890123")
                            .uuid("12345678-1234-1234-1234-123456789012")
                            .website(
                                Website.builder()
                                    .uuid("87654321-4321-4321-4321-876543210987")
                                    .build())
                            .build())))
            .build();

    when(urlAliasService.find(any(PageRequest.class))).thenReturn(expected);

    testJson(path, "/v5/urlaliases/find_with_result.json");
  }
}
