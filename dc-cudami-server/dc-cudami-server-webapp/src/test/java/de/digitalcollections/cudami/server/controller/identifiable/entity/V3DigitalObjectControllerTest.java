package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.DigitalObjectService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.CollectionBuilder;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.DigitalObjectBuilder;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.identifiable.entity.ProjectBuilder;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.paging.SearchPageResponseBuilder;
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

    SearchPageResponse<Collection> expected =
        (SearchPageResponse)
            new SearchPageResponseBuilder()
                .withoutContent()
                .forRequestPage(0)
                .forPageSize(1000)
                .forStartDate("c.publication_start", "2021-03-31")
                .forEndDate("c.publication_end", "2021-03-31")
                .build();

    DigitalObject digitalObject =
        new DigitalObjectBuilder().withUuid(extractFirstUuidFromPath(path)).build();
    when(digitalObjectService.getActiveCollections(eq(digitalObject), any(SearchPageRequest.class)))
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
    SearchPageResponse<Collection> expected =
        (SearchPageResponse)
            new SearchPageResponseBuilder<>()
                .forRequestPage(0)
                .forPageSize(1)
                .forStartDate("c.publication_start", "2021-04-12")
                .forEndDate("c.publication_end", "2021-04-12")
                .withTotalElements(1)
                .withContent(
                    new CollectionBuilder()
                        .createdAt("2020-07-16T11:51:33.981829")
                        .withLabel(Locale.GERMAN, "Lateinische Handschriften")
                        .withLabel(Locale.ENGLISH, "Latin Manuscripts")
                        .lastModifiedAt("2020-11-04T15:46:42.81741")
                        .withPreviewImage(
                            "Lateinische_Handschriften_bsb00131281_27.jpg",
                            "2780bee1-eeec-4b50-a95b-bba90793fc6a",
                            "file:///cudami/image/jpg/2780/bee1/eeec/4b50/a95b/bba9/0793/fc6a/resource.jpg",
                            MimeType.MIME_IMAGE_JPEG,
                            "https://api.digitale-sammlungen.de/iiif/image/v2/2780bee1-eeec-4b50-a95b-bba90793fc6a")
                        .withOpenPreviewImageInNewWindow()
                        .withUuid("25198d8b-38d4-49f7-9ef0-d99b3e607e30")
                        .withRefId(148)
                        .withPublicationStart("2020-11-01")
                        .build())
                .build();

    when(digitalObjectService.getActiveCollections(
            any(DigitalObject.class), any(SearchPageRequest.class)))
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
    SearchPageResponse<Project> expected =
        (SearchPageResponse)
            new SearchPageResponseBuilder<>()
                .forRequestPage(0)
                .forPageSize(1)
                .withTotalElements(1)
                .withContent(
                    new ProjectBuilder()
                        .createdAt("2020-07-16T11:51:33.981829")
                        .withLabel(Locale.GERMAN, "Lateinische Handschriften")
                        .withLabel(Locale.ENGLISH, "Latin Manuscripts")
                        .lastModifiedAt("2020-11-04T15:46:42.81741")
                        .withPreviewImage(
                            "Lateinische_Handschriften_bsb00131281_27.jpg",
                            "2780bee1-eeec-4b50-a95b-bba90793fc6a",
                            "file:///cudami/image/jpg/2780/bee1/eeec/4b50/a95b/bba9/0793/fc6a/resource.jpg",
                            MimeType.MIME_IMAGE_JPEG,
                            "https://api.digitale-sammlungen.de/iiif/image/v2/2780bee1-eeec-4b50-a95b-bba90793fc6a")
                        .withOpenPreviewImageInNewWindow()
                        .withUuid("25198d8b-38d4-49f7-9ef0-d99b3e607e30")
                        .withRefId(148)
                        .build())
                .build();

    when(digitalObjectService.getProjects(any(DigitalObject.class), any(SearchPageRequest.class)))
        .thenReturn(expected);

    testJson(path);
  }
}
