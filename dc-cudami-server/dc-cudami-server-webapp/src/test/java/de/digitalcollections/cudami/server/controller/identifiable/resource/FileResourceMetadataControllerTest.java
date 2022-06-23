package de.digitalcollections.cudami.server.controller.identifiable.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.FileResourceMetadataService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.resource.LinkedDataFileResource;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.FilterOperation;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(FileResourceMetadataController.class)
@DisplayName("The FileResourceMetadata controller")
class FileResourceMetadataControllerTest extends BaseControllerTest {

  @MockBean(name = "fileResourceMetadataService")
  private FileResourceMetadataService<FileResource> fileResourceMetadataService;

  @DisplayName("can return a filtered and paged list of LinkedDataFileResources")
  @ParameterizedTest
  @ValueSource(
      strings = {"/v6/fileresources?pageNumber=0&pageSize=1&uri=eq:http%3A%2F%2Ffoo.bar%2Fbla.xml"})
  public void find(String path) throws Exception {
    PageResponse<FileResource> expected =
        (PageResponse)
            PageResponse.builder()
                .forPageSize(1)
                .forAscendingOrderedField("label", "de")
                .forAscendingOrderedField("label")
                .withTotalElements(1)
                .withContent(
                    LinkedDataFileResource.builder()
                        .uuid("12345678-abcd-1234-abcd-123456789012")
                        .label(Locale.GERMAN, "Test-Label")
                        .mimeType(MimeType.MIME_APPLICATION_XML)
                        .uri("http://foo.bar/bla.xml")
                        .build())
                .build();

    when(fileResourceMetadataService.find(any(PageRequest.class))).thenReturn(expected);

    ArgumentCaptor<PageRequest> pageRequestArgumentCaptor =
        ArgumentCaptor.forClass(PageRequest.class);

    PageRequest expectedPageRequest = new PageRequest();
    expectedPageRequest.setPageSize(1);
    expectedPageRequest.setPageNumber(0);
    FilterCriterion filterCriterion =
        new FilterCriterion("uri", FilterOperation.EQUALS, "http://foo.bar/bla.xml");
    Filtering filtering = new Filtering(List.of(filterCriterion));
    expectedPageRequest.setFiltering(filtering);

    testJson(path, "/v6/fileresources/filteredlist.json");

    verify(fileResourceMetadataService, times(1)).find(pageRequestArgumentCaptor.capture());
    assertThat(pageRequestArgumentCaptor.getValue()).isEqualTo(expectedPageRequest);
  }

  @DisplayName("can retrieve by identifier with plaintext id")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v6/fileresources/identifier/foo:bar",
        "/v5/fileresources/identifier/foo:bar",
        "/v2/fileresources/identifier/foo:bar",
        "/latest/fileresources/identifier/foo:bar",
        "/v6/fileresources/identifier/foo:bar.json",
        "/v5/fileresources/identifier/foo:bar.json",
        "/v2/fileresources/identifier/foo:bar.json",
        "/latest/fileresources/identifier/foo:bar.json"
      })
  void testGetByIdentifierWithPlaintextId(String path) throws Exception {
    FileResource expected = FileResource.builder().build();

    when(fileResourceMetadataService.getByIdentifier(eq("foo"), eq("bar"))).thenReturn(expected);

    testHttpGet(path);

    verify(fileResourceMetadataService, times(1)).getByIdentifier(eq("foo"), eq("bar"));
  }

  @DisplayName("can retrieve by identifier with base 64 encoded data")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v6/fileresources/identifier/",
        "/v5/fileresources/identifier/",
        "/v2/fileresources/identifier/",
        "/latest/fileresources/identifier/"
      })
  void testGetByIdentifierWithBase64EncodedData(String basePath) throws Exception {
    FileResource expected = FileResource.builder().build();

    when(fileResourceMetadataService.getByIdentifier(eq("foo"), eq("bar/bla")))
        .thenReturn(expected);

    testHttpGet(
        basePath
            + Base64.getEncoder().encodeToString("foo:bar/bla".getBytes(StandardCharsets.UTF_8)));

    verify(fileResourceMetadataService, times(1)).getByIdentifier(eq("foo"), eq("bar/bla"));
  }
}
