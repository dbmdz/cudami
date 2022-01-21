package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.WebsiteService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.entity.WebsiteBuilder;
import de.digitalcollections.model.identifiable.web.WebpageBuilder;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.paging.SearchPageResponseBuilder;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(V2WebsiteController.class)
class V2WebsiteControllerTest extends BaseControllerTest {

  @MockBean private WebsiteService websiteService;

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
        new WebsiteBuilder()
            .createdAt("2018-05-04T09:05:47.493")
            .lastModifiedAt("2018-05-04T09:05:47.493")
            .withLabel(Locale.GERMAN, "Altsinica")
            .withDescription(Locale.GERMAN, "")
            .withUuid(extractFirstUuidFromPath(path))
            .withRefId(84)
            .withUrl("https://ostasien.digitale-sammlungen.de/")
            .withRootPages(
                List.of(
                    new WebpageBuilder()
                        .withUuid("6d9adace-187a-4f14-9a5a-e768558028a3")
                        .createdAt("2018-05-04T09:06:05.333")
                        .lastModifiedAt("2020-09-30T16:23:44.393791")
                        .withLabel(Locale.GERMAN, "Impressum")
                        .withPublicationStartAt("2020-09-30")
                        .build(),
                    new WebpageBuilder()
                        .withUuid("b0739393-2fdc-4703-8af1-c3b440292872")
                        .createdAt("2020-03-12T12:28:57.082438")
                        .lastModifiedAt("2020-09-30T16:24:43.844093")
                        .withLabel(Locale.GERMAN, "Barrierefreiheit")
                        .withPublicationStartAt("2020-09-30")
                        .build(),
                    new WebpageBuilder()
                        .withUuid("cbb85056-5e30-49cf-bd87-fd09486b9aa9")
                        .createdAt("2018-05-04T09:06:19.201")
                        .lastModifiedAt("2020-09-30T16:24:23.379512")
                        .withLabel(Locale.GERMAN, "Datenschutzerklärung")
                        .withPublicationStartAt("2020-09-30")
                        .build()))
            .build();

    when(websiteService.get(any(UUID.class))).thenReturn(expected);

    testJson(path);
  }

  @DisplayName("returns a paged list of websites")
  @ParameterizedTest
  @ValueSource(strings = {"/v2/websites?pageNumber=0&pageSize=1"})
  public void pagedWebsites(String path) throws Exception {
    SearchPageResponse<Website> expected =
        (SearchPageResponse)
            new SearchPageResponseBuilder<>()
                .forPageSize(1)
                .forRequestPage(0)
                .forAscendingOrderedField("label", "de")
                .forAscendingOrderedField("label")
                .withTotalElements(82)
                .withContent(
                    new WebsiteBuilder()
                        .createdAt("2018-05-02T13:32:52.582")
                        .withDescription(Locale.GERMAN, "")
                        .withLabel(Locale.GERMAN, "Testseite")
                        .lastModifiedAt("2018-09-11T09:47:40.311")
                        .withUuid("e91464a1-588b-434b-a88e-b6a1c3824c85")
                        .withRefId(71)
                        .withUrl("https://www.digitale-sammlungen.de/")
                        .build())
                .build();

    when(websiteService.find(any(SearchPageRequest.class))).thenReturn(expected);

    testJson(path);
  }
}
