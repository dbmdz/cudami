package de.digitalcollections.cudami.server.controller.identifiable.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
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
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(LinkedDataFileResourceController.class)
@DisplayName("The LinkedDataFileResource controller")
class LinkedDataFileResourceControllerTest extends BaseControllerTest {

  @MockBean(name = "linkedDataFileResourceService")
  private LinkedDataFileResourceService linkedDataFileResourceService;

  @DisplayName("can return a LinkedDataFileResource with its specific attributes")
  @ParameterizedTest
  @ValueSource(strings = {"/v6/linkeddatafileresources/12345678-abcd-1234-abcd-123456789012"})
  public void getByUuid(String path) throws Exception {
    LinkedDataFileResource expected =
        LinkedDataFileResource.builder()
            .uuid("12345678-abcd-1234-abcd-123456789012")
            .context("Test-context")
            .label(Locale.GERMAN, "Test-Label")
            .mimeType(MimeType.MIME_APPLICATION_XML)
            .uri("http://foo.bar/bla.xml")
            .objectType("LINKED_DATA")
            .build();

    when(linkedDataFileResourceService.getByExample(any(LinkedDataFileResource.class)))
        .thenReturn(expected);

    testJson(path, "/v6/linkeddatafileresources/12345678-abcd-1234-abcd-123456789012.json");
  }

  @DisplayName("can return a paged list of LinkedDataFileResources")
  @ParameterizedTest
  @ValueSource(strings = {"/v6/linkeddatafileresources/?pageNumber=0&pageSize=1"})
  public void pagedList(String path) throws Exception {
    PageResponse<LinkedDataFileResource> expected =
        PageResponse.builder()
            .forPageSize(1)
            .forRequestPage(0)
            .forAscendingOrderedField("label", "de")
            .forAscendingOrderedField("label")
            .withContent(
                List.of(
                    LinkedDataFileResource.builder()
                        .uuid("12345678-abcd-1234-abcd-123456789012")
                        .context("Test-context")
                        .label(Locale.GERMAN, "Test-Label")
                        .mimeType(MimeType.MIME_APPLICATION_XML)
                        .uri("http://foo.bar/bla.xml")
                        .build()))
            .build();

    when(linkedDataFileResourceService.find(any(PageRequest.class))).thenReturn(expected);

    testJson(path, "/v6/linkeddatafileresources/pagedlist.json");
  }

  @DisplayName("can return a filtered and paged list of LinkedDataFileResources")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v6/linkeddatafileresources/search?pageNumber=0&pageSize=1&filter=uri:eq:http://foo.bar/bla.xml"
      })
  public void find(String path) throws Exception {
    PageResponse<LinkedDataFileResource> expected =
        (PageResponse)
            PageResponse.builder()
                .forPageSize(1)
                .forAscendingOrderedField("label", "de")
                .forAscendingOrderedField("label")
                .withTotalElements(1)
                .withContent(
                    LinkedDataFileResource.builder()
                        .uuid("12345678-abcd-1234-abcd-123456789012")
                        .context("Test-context")
                        .objectType("LINKED_DATA")
                        .label(Locale.GERMAN, "Test-Label")
                        .mimeType(MimeType.MIME_APPLICATION_XML)
                        .uri("http://foo.bar/bla.xml")
                        .build())
                .build();

    when(linkedDataFileResourceService.find(any(PageRequest.class))).thenReturn(expected);

    ArgumentCaptor<PageRequest> pageRequestArgumentCaptor =
        ArgumentCaptor.forClass(PageRequest.class);

    PageRequest expectedPageRequest = new PageRequest();
    expectedPageRequest.setPageSize(1);
    expectedPageRequest.setPageNumber(0);
    FilterCriterion filterCriterion =
        new FilterCriterion("uri", FilterOperation.EQUALS, URI.create("http://foo.bar/bla.xml"));
    Filtering filtering = new Filtering(List.of(filterCriterion));
    expectedPageRequest.setFiltering(filtering);

    testJson(path, "/v6/linkeddatafileresources/filteredlist.json");

    verify(linkedDataFileResourceService, times(1)).find(pageRequestArgumentCaptor.capture());
    assertThat(pageRequestArgumentCaptor.getValue()).isEqualTo(expectedPageRequest);
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

    doAnswer(invocation -> replaceFirstArgumentData(expected, invocation))
        .when(linkedDataFileResourceService)
        .save(any(LinkedDataFileResource.class));

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

    doAnswer(invocation -> replaceFirstArgumentData(expected, invocation))
        .when(linkedDataFileResourceService)
        .update(any(LinkedDataFileResource.class));

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

  // ------------------------------------------------------------
  private static Object replaceFirstArgumentData(
      LinkedDataFileResource expected, InvocationOnMock invocation) {
    Object[] args = invocation.getArguments();
    ((LinkedDataFileResource) args[0]).setUuid(expected.getUuid());
    ((LinkedDataFileResource) args[0]).setLabel(expected.getLabel());
    ((LinkedDataFileResource) args[0]).setLastModified(expected.getLastModified());
    ((LinkedDataFileResource) args[0]).setUri(expected.getUri());
    ((LinkedDataFileResource) args[0]).setContext(expected.getContext());
    ((LinkedDataFileResource) args[0])
        .setPreviewImageRenderingHints(expected.getPreviewImageRenderingHints());
    return null;
  }
}
