package de.digitalcollections.cudami.server.controller.identifiable.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.LinkedDataFileResourceService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.resource.LinkedDataFileResource;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.FilterOperation;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(LinkedDataFileResourceController.class)
@DisplayName("The LinkedDataFileResource controller")
class LinkedDataFileResourceControllerTest extends BaseControllerTest {

  @MockBean(name = "linkedDataFileResourceService")
  private LinkedDataFileResourceService linkedDataFileResourceService;

  @DisplayName("can return a LinkedDataFileResource with its specific attributes")
  @ParameterizedTest
  @ValueSource(strings = {"/v5/linkeddatafileresources/12345678-abcd-1234-abcd-123456789012"})
  public void getByUuid(String path) throws Exception {
    LinkedDataFileResource expected =
        LinkedDataFileResource.builder()
            .uuid("12345678-abcd-1234-abcd-123456789012")
            .context("Test-context")
            .objectType("Test-objectType")
            .label(Locale.GERMAN, "Test-Label")
            .mimeType(MimeType.MIME_APPLICATION_XML)
            .uri("http://foo.bar/bla.xml")
            .build();

    when(linkedDataFileResourceService.getByUuid(any(UUID.class))).thenReturn(expected);

    testJson(path);
  }

  @DisplayName("can return a paged list of LinkedDataFileResources")
  @ParameterizedTest
  @ValueSource(strings = {"/v5/linkeddatafileresources/?pageNumber=0&pageSize=1"})
  public void pagedList(String path) throws Exception {
    PageResponse<LinkedDataFileResource> expected =
        PageResponse.builder()
            .forPageSize(1)
            .forRequestPage(0)
            .withContent(
                List.of(
                    LinkedDataFileResource.builder()
                        .uuid("12345678-abcd-1234-abcd-123456789012")
                        .context("Test-context")
                        .objectType("Test-objectType")
                        .label(Locale.GERMAN, "Test-Label")
                        .mimeType(MimeType.MIME_APPLICATION_XML)
                        .uri("http://foo.bar/bla.xml")
                        .build()))
            .build();

    when(linkedDataFileResourceService.find(any(PageRequest.class))).thenReturn(expected);

    testJson(path, "/v5/linkeddatafileresources/pagedlist.json");
  }

  @DisplayName("can return a filtered and paged list of LinkedDataFileResources")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v5/linkeddatafileresources/search?pageNumber=0&pageSize=1&uri=eq:http%3A%2F%2Ffoo.bar%2Fbla.xml"
      })
  public void find(String path) throws Exception {
    SearchPageResponse<LinkedDataFileResource> expected =
        (SearchPageResponse)
            SearchPageResponse.builder()
                .forPageSize(1)
                .forAscendingOrderedField("label", "de")
                .forAscendingOrderedField("label")
                .withTotalElements(1)
                .withContent(
                    LinkedDataFileResource.builder()
                        .uuid("12345678-abcd-1234-abcd-123456789012")
                        .context("Test-context")
                        .objectType("Test-objectType")
                        .label(Locale.GERMAN, "Test-Label")
                        .mimeType(MimeType.MIME_APPLICATION_XML)
                        .uri("http://foo.bar/bla.xml")
                        .build())
                .build();

    when(linkedDataFileResourceService.find(any(SearchPageRequest.class))).thenReturn(expected);

    ArgumentCaptor<SearchPageRequest> searchPageRequestArgumentCaptor =
        ArgumentCaptor.forClass(SearchPageRequest.class);

    SearchPageRequest expectedSearchPageRequest = new SearchPageRequest();
    expectedSearchPageRequest.setPageSize(1);
    expectedSearchPageRequest.setPageNumber(0);
    FilterCriterion filterCriterion =
        new FilterCriterion("uri", FilterOperation.EQUALS, "http://foo.bar/bla.xml");
    Filtering filtering = new Filtering(List.of(filterCriterion));
    expectedSearchPageRequest.setFiltering(filtering);

    testJson(path, "/v5/linkeddatafileresources/filteredlist.json");

    verify(linkedDataFileResourceService, times(1)).find(searchPageRequestArgumentCaptor.capture());
    assertThat(searchPageRequestArgumentCaptor.getValue()).isEqualTo(expectedSearchPageRequest);
  }

  @DisplayName("successfully creates a LinkedDataFileResource")
  @ParameterizedTest
  @ValueSource(strings = {"/v5/linkeddatafileresources"})
  public void save(String path) throws Exception {
    LinkedDataFileResource expected =
        LinkedDataFileResource.builder()
            .uuid("12345678-abcd-1234-abcd-123456789012")
            .context("Test-context")
            .objectType("Test-objectType")
            .label(Locale.GERMAN, "Test-Label")
            .mimeType(MimeType.MIME_APPLICATION_XML)
            .uri("http://foo.bar/bla.xml")
            .build();

    when(linkedDataFileResourceService.save(any(LinkedDataFileResource.class)))
        .thenReturn(expected);

    // The body is the serialized LinkedDataFileResource to be persisted
    String body =
        "{\n"
            + "  \"type\": \"RESOURCE\",\n"
            + "  \"fileResourceType\": \"LINKED_DATA\",\n"
            + "  \"filename\": \"bla.xml\",\n"
            + "  \"mimeType\": \"application/xml\",\n"
            + "  \"readonly\": false,\n"
            + "  \"sizeInBytes\": 0,\n"
            + "  \"context\": \"Test-Context\",\n"
            + "  \"objectType\": \"Test-objectType\",\n"
            + "  \"filenameExtension\": \"xml\"\n"
            + "}";

    testPostJson(
        path, body, "/v5/linkeddatafileresources/12345678-abcd-1234-abcd-123456789012.json");
  }

  @DisplayName("successfully updates a LinkedDataFileResource by its uuid")
  @ParameterizedTest
  @ValueSource(strings = {"/v5/linkeddatafileresources/12345678-abcd-1234-abcd-123456789012"})
  public void update(String path) throws Exception {
    LinkedDataFileResource expected =
        LinkedDataFileResource.builder()
            .uuid("12345678-abcd-1234-abcd-123456789012")
            .context("Test-context")
            .objectType("Test-objectType")
            .label(Locale.GERMAN, "Test-Label")
            .mimeType(MimeType.MIME_APPLICATION_XML)
            .uri("http://foo.bar/bla.xml")
            .build();

    when(linkedDataFileResourceService.update(any(LinkedDataFileResource.class)))
        .thenReturn(expected);

    // The body is the serialized LinkedDataFileResource to be persisted
    String body =
        "{\n"
            + "  \"uuid\": \"12345678-abcd-1234-abcd-123456789012\",\n"
            + "  \"type\": \"RESOURCE\",\n"
            + "  \"fileResourceType\": \"LINKED_DATA\",\n"
            + "  \"filename\": \"bla.xml\",\n"
            + "  \"mimeType\": \"application/xml\",\n"
            + "  \"readonly\": false,\n"
            + "  \"sizeInBytes\": 0,\n"
            + "  \"context\": \"Test-Context\",\n"
            + "  \"objectType\": \"Test-objectType\",\n"
            + "  \"filenameExtension\": \"xml\"\n"
            + "}";

    testPutJson(
        path, body, "/v5/linkeddatafileresources/12345678-abcd-1234-abcd-123456789012.json");
  }
}
