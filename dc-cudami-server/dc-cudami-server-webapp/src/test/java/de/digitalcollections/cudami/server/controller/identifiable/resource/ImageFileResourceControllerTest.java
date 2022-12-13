package de.digitalcollections.cudami.server.controller.identifiable.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.ImageFileResourceService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.FilterOperation;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(ImageFileResourceController.class)
@DisplayName("The ImageFileResource controller")
class ImageFileResourceControllerTest extends BaseControllerTest {

  @MockBean(name = "imageFileResourceService")
  private ImageFileResourceService imageFileResourceService;

  @DisplayName("can return an ImageFileResource with its specific attributes")
  @ParameterizedTest
  @ValueSource(strings = {"/v6/imagefileresources/12345678-abcd-1234-abcd-123456789012"})
  public void getByUuid(String path) throws Exception {
    ImageFileResource expected =
        ImageFileResource.builder()
            .uuid("12345678-abcd-1234-abcd-123456789012")
            .altText(Locale.GERMAN, "Vorschaubild")
            .label(Locale.GERMAN, "Test-Label")
            .mimeType(MimeType.MIME_IMAGE_JPEG)
            .uri("http://foo.bar/baz.jpg")
            .filename("baz.jpg")
            .build();

    when(imageFileResourceService.getByUuid(any(UUID.class))).thenReturn(expected);

    testJson(path, "/v6/imagefileresources/12345678-abcd-1234-abcd-123456789012.json");
  }

  @DisplayName("can return a paged list of ImageFileResources")
  @ParameterizedTest
  @ValueSource(strings = {"/v6/imagefileresources/?pageNumber=0&pageSize=1"})
  public void pagedList(String path) throws Exception {
    PageResponse<ImageFileResource> expected =
        PageResponse.builder()
            .forPageSize(1)
            .forRequestPage(0)
            .forAscendingOrderedField("label", "de")
            .forAscendingOrderedField("label")
            .withContent(
                List.of(
                    ImageFileResource.builder()
                        .uuid("12345678-abcd-1234-abcd-123456789012")
                        .altText(Locale.GERMAN, "Vorschaubild")
                        .label(Locale.GERMAN, "Test-Label")
                        .mimeType(MimeType.MIME_IMAGE_JPEG)
                        .uri("http://foo.bar/baz.jpg")
                        .filename("baz.jpg")
                        .build()))
            .build();

    when(imageFileResourceService.find(any(PageRequest.class))).thenReturn(expected);

    testJson(path, "/v6/imagefileresources/pagedlist.json");
  }

  @DisplayName("can return a filtered and paged list of ImageFileResources")
  @ParameterizedTest
  @ValueSource(
      strings = {"/v6/imagefileresources/search?pageNumber=0&pageSize=1&filename=eq:bla.jpg"})
  public void find(String path) throws Exception {
    PageResponse<ImageFileResource> expected =
        (PageResponse)
            PageResponse.builder()
                .forPageSize(1)
                .forAscendingOrderedField("label", "de")
                .forAscendingOrderedField("label")
                .withTotalElements(1)
                .withContent(
                    ImageFileResource.builder()
                        .uuid("12345678-abcd-1234-abcd-123456789012")
                        .altText(Locale.GERMAN, "Vorschaubild")
                        .label(Locale.GERMAN, "Test-Label")
                        .mimeType(MimeType.MIME_IMAGE_JPEG)
                        .uri("http://foo.bar/baz.jpg")
                        .filename("baz.jpg")
                        .build())
                .build();

    when(imageFileResourceService.find(any(PageRequest.class))).thenReturn(expected);

    ArgumentCaptor<PageRequest> pageRequestArgumentCaptor =
        ArgumentCaptor.forClass(PageRequest.class);

    PageRequest expectedPageRequest = new PageRequest();
    expectedPageRequest.setPageSize(1);
    expectedPageRequest.setPageNumber(0);
    FilterCriterion filterCriterion =
        new FilterCriterion("filename", FilterOperation.EQUALS, "bla.jpg");
    Filtering filtering = new Filtering(List.of(filterCriterion));
    expectedPageRequest.setFiltering(filtering);

    testJson(path, "/v6/imagefileresources/filteredlist.json");

    verify(imageFileResourceService, times(1)).find(pageRequestArgumentCaptor.capture());
    assertThat(pageRequestArgumentCaptor.getValue()).isEqualTo(expectedPageRequest);
  }

  @DisplayName("successfully creates an ImageFileResource")
  @ParameterizedTest
  @ValueSource(strings = {"/v6/imagefileresources"})
  public void save(String path) throws Exception {
    ImageFileResource expected =
        ImageFileResource.builder()
            .uuid("12345678-abcd-1234-abcd-123456789012")
            .altText(Locale.GERMAN, "Vorschaubild")
            .label(Locale.GERMAN, "Test-Label")
            .mimeType(MimeType.MIME_IMAGE_JPEG)
            .uri("http://foo.bar/baz.jpg")
            .filename("baz.jpg")
            .build();

    // FIXME when(imageFileResourceService.save(any(ImageFileResource.class))).thenReturn(expected);
    // Das muss irgendwie mit doAnswer o.ä, gemocked werden, weil der Aufruf der void
    // save-Methode das übergebene Argument ändert (auf "expected")

    /*
    Theorie:

    doAnswer( invocation -> {
      ImageFileResource savedObject = invocation.getArgument(0);
      savedObject = expected;
      return invocation;
    }).when(imageFileResourceService).save(any(ImageFileResource.class));

    Praxis: Läuft nicht
     */

    // The body is the serialized LinkedDataFileResource to be persisted
    String body =
        "{\n"
            + "  \"type\": \"RESOURCE\",\n"
            + "  \"fileResourceType\": \"IMAGE\",\n"
            + "  \"filename\": \"baz.jpg\",\n"
            + "  \"mimeType\": \"image/jpeg\",\n"
            + "  \"readonly\": false,\n"
            + "  \"sizeInBytes\": 0,\n"
            + "  \"filenameExtension\": \"jpg\"\n"
            + "}";

    testPostJson(path, body, "/v6/imagefileresources/12345678-abcd-1234-abcd-123456789012.json");
  }

  @DisplayName("successfully updates an ImageFileResource by its uuid")
  @ParameterizedTest
  @ValueSource(strings = {"/v6/imagefileresources/12345678-abcd-1234-abcd-123456789012"})
  public void update(String path) throws Exception {
    ImageFileResource expected =
        ImageFileResource.builder()
            .uuid("12345678-abcd-1234-abcd-123456789012")
            .altText(Locale.GERMAN, "Vorschaubild")
            .label(Locale.GERMAN, "Test-Label")
            .mimeType(MimeType.MIME_IMAGE_JPEG)
            .uri("http://foo.bar/baz.jpg")
            .filename("baz.jpg")
            .build();

    // FIXME
    // when(imageFileResourceService.update(any(ImageFileResource.class))).thenReturn(expected);

    // The body is the serialized LinkedDataFileResource to be persisted
    String body =
        "{\n"
            + "  \"uuid\": \"12345678-abcd-1234-abcd-123456789012\",\n"
            + "  \"type\": \"RESOURCE\",\n"
            + "  \"fileResourceType\": \"IMAGE\",\n"
            + "  \"filename\": \"baz.jpg\",\n"
            + "  \"mimeType\": \"image/jpeg\",\n"
            + "  \"readonly\": false,\n"
            + "  \"sizeInBytes\": 0,\n"
            + "  \"filenameExtension\": \"jpg\"\n"
            + "}";

    testPutJson(path, body, "/v6/imagefileresources/12345678-abcd-1234-abcd-123456789012.json");
  }

  @DisplayName("can retrieve by identifier with plaintext id")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v6/imagefileresources/identifier/foo:bar",
        "/v6/imagefileresources/identifier/foo:bar.json"
      })
  void testGetByIdentifierWithPlaintextId(String path) throws Exception {
    ImageFileResource expected = ImageFileResource.builder().build();

    when(imageFileResourceService.getByIdentifier(eq("foo"), eq("bar"))).thenReturn(expected);

    testHttpGet(path);

    verify(imageFileResourceService, times(1)).getByIdentifier(eq("foo"), eq("bar"));
  }

  @DisplayName("can retrieve by identifier with base 64 encoded data")
  @ParameterizedTest
  @ValueSource(strings = {"/v6/imagefileresources/identifier/"})
  void testGetByIdentifierWithBase64EncodedData(String basePath) throws Exception {
    ImageFileResource expected = ImageFileResource.builder().build();

    when(imageFileResourceService.getByIdentifier(eq("foo"), eq("bar/bla"))).thenReturn(expected);

    testHttpGet(
        basePath
            + Base64.getEncoder().encodeToString("foo:bar/bla".getBytes(StandardCharsets.UTF_8)));

    verify(imageFileResourceService, times(1)).getByIdentifier(eq("foo"), eq("bar/bla"));
  }
}
