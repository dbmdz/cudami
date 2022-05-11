package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.ArticleService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.entity.Article;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(V5ArticleController.class)
@DisplayName("The V5 ArticleController")
class V5ArticleControllerTest extends BaseControllerTest {

  @MockBean private ArticleService articleService;

  @DisplayName("shall return a paged list of articles")
  @ParameterizedTest
  @ValueSource(strings = {"/v5/articles?pageSize=1&pageNumber=0"})
  void testFind(String path) throws Exception {
    PageResponse<Article> expected =
        (PageResponse<Article>)
            PageResponse.builder()
                .forRequestPage(0)
                .forPageSize(1)
                .withTotalElements(4)
                .forDescendingOrderedField("lastModified")
                .forAscendingOrderedField("uuid")
                .withContent(
                    List.of(
                        Article.builder()
                            .created("2020-02-10T00:00:00.000000")
                            .lastModified("2020-02-10T00:00:00.000000")
                            .uuid("32638dd2-28ce-4cf1-af65-e998060426d3")
                            .description(Locale.GERMAN, "Test-Text")
                            .label(Locale.GERMAN, "Test-Artikel")
                            .build()))
                .build();

    when(articleService.find(any(PageRequest.class))).thenReturn(expected);

    testJson(path, "/v5/articles/find_with_result.json");
  }
}
