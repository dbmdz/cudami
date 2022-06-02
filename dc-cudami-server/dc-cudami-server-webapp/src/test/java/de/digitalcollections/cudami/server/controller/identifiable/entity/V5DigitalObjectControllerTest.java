package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.DigitalObjectService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.FilterOperation;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(V5DigitalObjectController.class)
@DisplayName("The V5 DigitalObjectController")
class V5DigitalObjectControllerTest extends BaseControllerTest {

  @MockBean private DigitalObjectService digitalObjectService;

  @DisplayName("shall return a paged list of digital objects")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v5/digitalobjects/search?pageSize=1&pageNumber=0",
        "/v3/digitalobjects/search?pageSize=1&pageNumber=0",
        "/latest/digitalobjects/search?pageSize=1&pageNumber=0"
      })
  void testFind(String path) throws Exception {
    PageResponse<DigitalObject> expected =
        PageResponse.builder()
            .forRequestPage(0)
            .forPageSize(1)
            .withTotalElements(1365676)
            .forAscendingOrderedField("label", "de")
            .forAscendingOrderedField("label")
            .forDescendingOrderedField("uuid")
            .withContent(
                List.of(
                    DigitalObject.builder()
                        .created("2020-10-14T00:00:00")
                        .lastModified("2020-10-14T00:00:00")
                        .uuid("e2e75cd3-87c3-4b70-b29a-58086ce5ad4d")
                        .label(Locale.GERMAN, "Testdigitalisat")
                        .identifier(
                            "mdz-obj", "bsb12345678", "4bbe38a4-96e9-4200-9360-740da00f104f")
                        .refId(37423)
                        .build()))
            .build();

    when(digitalObjectService.find(any(PageRequest.class))).thenReturn(expected);

    testJson(path, "/v5/digitalobjects/find_with_result.json");
  }

  @DisplayName("shall return a page list of projects for a digital object")
  @ParameterizedTest
  @ValueSource(strings = {"/v5/digitalobjects/e2e75cd3-87c3-4b70-b29a-58086ce5ad4d/projects"})
  void testFindProjects(String path) throws Exception {
    PageResponse<Project> expected =
        PageResponse.builder()
            .forRequestPage(0)
            .forPageSize(1)
            .withTotalElements(1)
            .withContent(
                List.of(
                    Project.builder()
                        .created("2020-10-15T00:00:00")
                        .lastModified("2020-10-15T00:00:00")
                        .label(Locale.GERMAN, "Testprojekt")
                        .identifier(
                            "mdz-proj", "1245413523", "95545a10-feb2-4a4f-a88d-3124f47f6f06")
                        .uuid("5c6b2788-94a5-482a-8471-d78513c905db")
                        .refId(590334)
                        .build()))
            .build();

    when(digitalObjectService.findProjects(any(DigitalObject.class), any(PageRequest.class)))
        .thenReturn(expected);

    testJson(path, "/v5/digitalobjects/e2e75cd3-87c3-4b70-b29a-58086ce5ad4d_projects.json");
  }

  @DisplayName("shall return a page list of collections for a digital object")
  @ParameterizedTest
  @ValueSource(strings = {"/v5/digitalobjects/e2e75cd3-87c3-4b70-b29a-58086ce5ad4d/collections"})
  void testFindCollections(String path) throws Exception {
    PageResponse<Collection> expected =
        PageResponse.builder()
            .forRequestPage(0)
            .forPageSize(1)
            .withTotalElements(1)
            .withContent(
                List.of(
                    Collection.builder()
                        .created("2020-03-03T00:00:00")
                        .lastModified("2020-03-03T00:00:00")
                        .label(Locale.GERMAN, "Testcollection")
                        .uuid("96f02509-478f-4d80-a6ba-bd68acf9e23a")
                        .refId(16)
                        .publicationStart("2020-10-01")
                        .build()))
            .build();

    when(digitalObjectService.findCollections(any(DigitalObject.class), any(PageRequest.class)))
        .thenReturn(expected);

    testJson(path, "/v5/digitalobjects/e2e75cd3-87c3-4b70-b29a-58086ce5ad4d_collections.json");
  }

  @DisplayName("can filter by the parent UUID")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v5/digitalobjects?pageNumber=0&pageSize=10000&parent.uuid=eq:1c419226-8d61-4efa-923a-7fbaf961eb9d"
      })
  public void filterByParentUUID(String path) throws Exception {
    UUID parentUuid = UUID.fromString("1c419226-8d61-4efa-923a-7fbaf961eb9d");

    PageRequest expectedPageRequest = new PageRequest();
    expectedPageRequest.setPageSize(10000);
    expectedPageRequest.setPageNumber(0);
    FilterCriterion filterCriterion =
        new FilterCriterion("parent.uuid", FilterOperation.EQUALS, parentUuid);
    Filtering filtering = new Filtering(List.of(filterCriterion));
    expectedPageRequest.setFiltering(filtering);

    PageResponse<DigitalObject> expected = new PageResponse<>();
    expected.setContent(
        List.of(
            DigitalObject.builder()
                .uuid("7593c90e-6fb7-49b4-a70b-032761c9bbcd")
                .created("2020-08-21T07:49:37.004443")
                .identifier("mdz-obj", "bsb10000001", "53e3e619-47a3-4110-84f7-acba12a52298")
                .label("Label")
                .lastModified("2020-08-21T07:49:37.00445")
                .previewImage(
                    "default.jpg",
                    "abe16b03-c5d5-41a6-9475-f742e06ae881",
                    "https://api-dev.digitale-sammlungen.de/iiif/image/v2/bsb10000001_00003/full/250,/0/default.jpg")
                .refId(72)
                .parent(DigitalObject.builder().uuid(parentUuid).build())
                .build()));
    expected.setRequest(expectedPageRequest);

    when(digitalObjectService.find(any(PageRequest.class))).thenReturn(expected);

    ArgumentCaptor<PageRequest> pageRequestArgumentCaptor =
        ArgumentCaptor.forClass(PageRequest.class);

    testJson(path, "/v5/digitalobjects/filtered_by_parent.json");

    verify(digitalObjectService, times(1)).find(pageRequestArgumentCaptor.capture());
    assertThat(pageRequestArgumentCaptor.getValue()).isEqualTo(expectedPageRequest);
  }
}
