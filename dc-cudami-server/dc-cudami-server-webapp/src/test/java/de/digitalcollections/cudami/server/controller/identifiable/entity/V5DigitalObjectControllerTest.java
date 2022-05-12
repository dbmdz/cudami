package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.DigitalObjectService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
        (PageResponse<DigitalObject>)
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
        (PageResponse<Project>)
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
        (PageResponse<Collection>)
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
}
