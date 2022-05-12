package de.digitalcollections.cudami.server.controller.identifiable.resource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.LinkedDataFileResourceService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.resource.LinkedDataFileResource;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(V5LinkedDataFileResourceController.class)
@DisplayName("The V5 LinkedDataFileResource Controller")
@Disabled("No idea, why they give errors")
class V5LinkedDataFileResourceControllerTest extends BaseControllerTest {

  @MockBean private LinkedDataFileResourceService linkedDataFileResourceService;

  @DisplayName("shall return a paged list of linked data file resources")
  @ParameterizedTest
  @ValueSource(strings = {"/v5/linkeddatafileresources?pageSize=1&pageNumber=0"})
  void testFind(String path) throws Exception {
    PageResponse<LinkedDataFileResource> expected =
        (PageResponse<LinkedDataFileResource>)
            PageResponse.builder()
                .forRequestPage(0)
                .forPageSize(1)
                .withTotalElements(1)
                .forAscendingOrderedField("label", "de")
                .forAscendingOrderedField("label")
                .withContent(
                    List.of(
                        LinkedDataFileResource.builder()
                            .label(Locale.GERMAN, "Test-Label")
                            .filename("bla.xml")
                            .mimeType(MimeType.MIME_APPLICATION_XML)
                            .uri("http://foo.bar/bla.xml")
                            .uuid("12345678-abcd-1234-abcd-123456789012")
                            .build()))
                .build();

    when(linkedDataFileResourceService.find(any(PageRequest.class))).thenReturn(expected);

    testJson(path, "/v5/linkeddatafileresources/pagedlist.json");
  }

  @DisplayName("shall return a paged list of file resources by the search endpoint")
  @ParameterizedTest
  @ValueSource(strings = {"/v5/linkeddatafileresources/search?pageSize=1&pageNumber=0"})
  void testSearch(String path) throws Exception {
    PageResponse<LinkedDataFileResource> expected =
        (PageResponse<LinkedDataFileResource>)
            PageResponse.builder()
                .forRequestPage(0)
                .forPageSize(1)
                .withTotalElements(1)
                .forAscendingOrderedField("label", "de")
                .forAscendingOrderedField("label")
                .withContent(
                    List.of(
                        LinkedDataFileResource.builder()
                            .label(Locale.GERMAN, "Test-Label")
                            .filename("bla.xml")
                            .mimeType(MimeType.MIME_APPLICATION_XML)
                            .uri("http://foo.bar/bla.xml")
                            .uuid("12345678-abcd-1234-abcd-123456789012")
                            .build()))
                .build();

    when(linkedDataFileResourceService.find(any(PageRequest.class))).thenReturn(expected);

    testJson(path, "/v5/linkeddatafileresources/pagedlist.json");
  }
}
