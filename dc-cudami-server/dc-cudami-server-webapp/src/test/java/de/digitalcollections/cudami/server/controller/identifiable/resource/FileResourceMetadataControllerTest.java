package de.digitalcollections.cudami.server.controller.identifiable.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.FileResourceMetadataService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.filter.FilterCriterion;
import de.digitalcollections.model.filter.FilterOperation;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.resource.PlainFileResourceBuilder;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.paging.SearchPageResponseBuilder;
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
      strings = {"/v5/fileresources?pageNumber=0&pageSize=1&uri=eq:http%3A%2F%2Ffoo.bar%2Fbla.xml"})
  public void find(String path) throws Exception {
    SearchPageResponse<FileResource> expected =
        (SearchPageResponse)
            new SearchPageResponseBuilder<>()
                .forPageSize(1)
                .forAscendingOrderedField("label", "de")
                .forAscendingOrderedField("label")
                .withTotalElements(1)
                .withContent(
                    new PlainFileResourceBuilder()
                        .withUuid("12345678-abcd-1234-abcd-123456789012")
                        .withLabel(Locale.GERMAN, "Test-Label")
                        .withMimeType(MimeType.MIME_APPLICATION_XML)
                        .withUri("http://foo.bar/bla.xml")
                        .build())
                .build();

    when(fileResourceMetadataService.find(any(SearchPageRequest.class))).thenReturn(expected);

    ArgumentCaptor<SearchPageRequest> searchPageRequestArgumentCaptor =
        ArgumentCaptor.forClass(SearchPageRequest.class);

    SearchPageRequest expectedSearchPageRequest = new SearchPageRequest();
    expectedSearchPageRequest.setPageSize(1);
    expectedSearchPageRequest.setPageNumber(0);
    FilterCriterion filterCriterion =
        new FilterCriterion("uri", FilterOperation.EQUALS, "http://foo.bar/bla.xml");
    Filtering filtering = new Filtering(List.of(filterCriterion));
    expectedSearchPageRequest.setFiltering(filtering);

    testJson(path, "/v5/fileresources/filteredlist.json");

    verify(fileResourceMetadataService, times(1)).find(searchPageRequestArgumentCaptor.capture());
    assertThat(searchPageRequestArgumentCaptor.getValue()).isEqualTo(expectedSearchPageRequest);
  }
}
