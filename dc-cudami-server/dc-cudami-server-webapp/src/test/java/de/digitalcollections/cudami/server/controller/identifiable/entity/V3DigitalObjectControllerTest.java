package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.DigitalObjectService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(V3DigitalObjectController.class)
@DisplayName("The DigitalObjectController V3")
public class V3DigitalObjectControllerTest extends BaseControllerTest {

  @MockBean private DigitalObjectService digitalObjectService;

  @DisplayName("can return empty collections for a digital object")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/latest/digitalobjects/51f9e6c9-4c91-4fdc-9563-26c17ff110cc/collections?active=true&pageNumber=0&pageSize=1000"
      })
  public void emptyCollectionsForDigitalObject(String path) throws Exception {

    PageResponse<Collection> expected =
        (PageResponse)
            PageResponse.builder()
                .withoutContent()
                .forRequestPage(0)
                .forPageSize(1000)
                .forStartDate("c.publication_start", "2021-03-31")
                .forEndDate("c.publication_end", "2021-03-31")
                .build();

    DigitalObject digitalObject =
        DigitalObject.builder().uuid(extractFirstUuidFromPath(path)).build();
    when(digitalObjectService.findActiveCollections(eq(digitalObject), any(PageRequest.class)))
        .thenReturn(expected);

    testJson(path);
  }

  @DisplayName("can return the collections, a digital object belongs to")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v3/digitalobjects/6bfbe6dc-2c14-4e61-b88b-ce56cea712c7/collections?active=true&pageNumber=0&pageSize=1"
      })
  public void collectionsForDigitalObject(String path) throws Exception {
    PageResponse<Collection> expected =
        (PageResponse)
            PageResponse.builder()
                .forRequestPage(0)
                .forPageSize(1)
                .forStartDate("c.publication_start", "2021-04-12")
                .forEndDate("c.publication_end", "2021-04-12")
                .withTotalElements(1)
                .withContent(
                    Collection.builder()
                        .created("2020-07-16T11:51:33.981829")
                        .label(Locale.GERMAN, "Lateinische Handschriften")
                        .label(Locale.ENGLISH, "Latin Manuscripts")
                        .lastModified("2020-11-04T15:46:42.81741")
                        .previewImage(
                            "Lateinische_Handschriften_bsb00131281_27.jpg",
                            "2780bee1-eeec-4b50-a95b-bba90793fc6a",
                            "file:///cudami/image/jpg/2780/bee1/eeec/4b50/a95b/bba9/0793/fc6a/resource.jpg",
                            MimeType.MIME_IMAGE_JPEG,
                            "https://api.digitale-sammlungen.de/iiif/image/v2/2780bee1-eeec-4b50-a95b-bba90793fc6a")
                        .openPreviewImageInNewWindow()
                        .uuid("25198d8b-38d4-49f7-9ef0-d99b3e607e30")
                        .refId(148)
                        .publicationStart("2020-11-01")
                        .build())
                .build();

    when(digitalObjectService.findActiveCollections(
            any(DigitalObject.class), any(PageRequest.class)))
        .thenReturn(expected);

    testJson(path);
  }

  @DisplayName("can return the projects, a digital object belongs to")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v3/digitalobjects/6bfbe6dc-2c14-4e61-b88b-ce56cea712c7/projects?pageNumber=0&pageSize=1"
      })
  public void projectsForDigitalObject(String path) throws Exception {
    PageResponse<Project> expected =
        (PageResponse)
            PageResponse.builder()
                .forRequestPage(0)
                .forPageSize(1)
                .withTotalElements(1)
                .withContent(
                    Project.builder()
                        .created("2020-07-16T11:51:33.981829")
                        .label(Locale.GERMAN, "Lateinische Handschriften")
                        .label(Locale.ENGLISH, "Latin Manuscripts")
                        .lastModified("2020-11-04T15:46:42.81741")
                        .previewImage(
                            "Lateinische_Handschriften_bsb00131281_27.jpg",
                            "2780bee1-eeec-4b50-a95b-bba90793fc6a",
                            "file:///cudami/image/jpg/2780/bee1/eeec/4b50/a95b/bba9/0793/fc6a/resource.jpg",
                            MimeType.MIME_IMAGE_JPEG,
                            "https://api.digitale-sammlungen.de/iiif/image/v2/2780bee1-eeec-4b50-a95b-bba90793fc6a")
                        .openPreviewImageInNewWindow()
                        .uuid("25198d8b-38d4-49f7-9ef0-d99b3e607e30")
                        .refId(148)
                        .build())
                .build();

    when(digitalObjectService.findProjects(any(DigitalObject.class), any(PageRequest.class)))
        .thenReturn(expected);

    testJson(path);
  }
}
