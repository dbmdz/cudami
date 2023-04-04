package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.WebsiteService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(V2WebsiteController.class)
class V2WebsiteControllerTest extends BaseControllerTest {

  @MockBean private WebsiteService websiteService;

  @DisplayName("returns a paged list of websites")
  @ParameterizedTest
  @ValueSource(strings = {"/v2/websites?pageNumber=0&pageSize=1"})
  public void pagedWebsites(String path) throws Exception {
    PageResponse<Website> expected =
        (PageResponse)
            PageResponse.builder()
                .forPageSize(1)
                .forRequestPage(0)
                .forAscendingOrderedField("label", "de")
                .forAscendingOrderedField("label")
                .withTotalElements(82)
                .withContent(
                    Website.builder()
                        .created("2018-05-02T13:32:52.582")
                        .description(Locale.GERMAN, "")
                        .label(Locale.GERMAN, "Testseite")
                        .lastModified("2018-09-11T09:47:40.311")
                        .uuid("e91464a1-588b-434b-a88e-b6a1c3824c85")
                        .refId(71)
                        .url("https://www.digitale-sammlungen.de/")
                        .build())
                .build();

    when(websiteService.find(any(PageRequest.class))).thenReturn(expected);

    testJson(path);
  }

  @DisplayName(
      "returns a website in v2 json format for UUID, with or without json suffix in the url")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v2/websites/7ebaf4b1-cf5a-491b-991c-4fd082677ff9.json",
        "/v2/websites/7ebaf4b1-cf5a-491b-991c-4fd082677ff9"
      })
  public void returnWebsiteV2Json(String path) throws Exception {
    Website expected =
        Website.builder()
            .created("2018-05-04T09:05:47.493")
            .lastModified("2018-05-04T09:05:47.493")
            .label(Locale.GERMAN, "Altsinica")
            .description(Locale.GERMAN, "")
            .uuid(extractFirstUuidFromPath(path))
            .refId(84)
            .url("https://ostasien.digitale-sammlungen.de/")
            .rootPages(
                List.of(
                    Webpage.builder()
                        .uuid("6d9adace-187a-4f14-9a5a-e768558028a3")
                        .created("2018-05-04T09:06:05.333")
                        .lastModified("2020-09-30T16:23:44.393791")
                        .label(Locale.GERMAN, "Impressum")
                        .publicationStart("2020-09-30")
                        .build(),
                    Webpage.builder()
                        .uuid("b0739393-2fdc-4703-8af1-c3b440292872")
                        .created("2020-03-12T12:28:57.082438")
                        .lastModified("2020-09-30T16:24:43.844093")
                        .label(Locale.GERMAN, "Barrierefreiheit")
                        .publicationStart("2020-09-30")
                        .build(),
                    Webpage.builder()
                        .uuid("cbb85056-5e30-49cf-bd87-fd09486b9aa9")
                        .created("2018-05-04T09:06:19.201")
                        .lastModified("2020-09-30T16:24:23.379512")
                        .label(Locale.GERMAN, "Datenschutzerklärung")
                        .publicationStart("2020-09-30")
                        .build()))
            .build();

    when(websiteService.getByExample(any(Website.class))).thenReturn(expected);

    testJson(path);
  }
}
