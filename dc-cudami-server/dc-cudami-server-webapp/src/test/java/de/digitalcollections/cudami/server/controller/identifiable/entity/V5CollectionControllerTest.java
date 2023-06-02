package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.CollectionService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.FilterLogicalOperator;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(V5CollectionController.class)
@DisplayName("The V5 CollectionController")
class V5CollectionControllerTest extends BaseControllerTest {

  @MockBean private CollectionService collectionService;

  @MockBean private LocaleService localeService;

  @DisplayName("shall return a paged list of collections")
  @ParameterizedTest
  @ValueSource(
      strings = {"/v5/collections/search?pageSize=1&pageNumber=0&searchTerm=Test-Sammlung"})
  void testFind(String path) throws Exception {
    PageResponse<Collection> expected =
        PageResponse.builder()
            .forRequestPage(0)
            .forPageSize(1)
            .withTotalElements(156)
            .forDescendingOrderedField("lastModified")
            .forAscendingOrderedField("uuid")
            .withContent(
                List.of(
                    Collection.builder()
                        .created("2022-03-31T11:32:04.189939")
                        .lastModified("2022-05-02T16:18:52.710802")
                        .uuid("900fbf70-12de-4483-a0e3-9d77ea49626e")
                        .label(Locale.GERMAN, "Test-Sammlung")
                        .publicationStart("2022-05-02")
                        .build()))
            .build();
    expected
        .getRequest()
        .add(
            Filtering.builder()
                .filterCriterion(
                    FilterLogicalOperator.OR,
                    FilterCriterion.builder()
                        .withExpression("label")
                        .isEquals("Test-Sammlung")
                        .build())
                .filterCriterion(
                    FilterLogicalOperator.OR,
                    FilterCriterion.builder()
                        .withExpression("description")
                        .isEquals("Test-Sammlung")
                        .build())
                .build());

    when(collectionService.find(any(PageRequest.class))).thenReturn(expected);

    testJson(path, "/v5/collections/find_with_result_and_query.json");
  }

  @DisplayName("shall return a paged list of digital objects for a collections")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v5/collections/900fbf70-12de-4483-a0e3-9d77ea49626e/digitalobjects?pageSize=1&pageNumber=0"
      })
  void testFindDigitalobjects(String path) throws Exception {
    PageResponse<DigitalObject> expected =
        PageResponse.builder()
            .forRequestPage(0)
            .forPageSize(1)
            .withTotalElements(71)
            .forDescendingOrderedField("lastModified")
            .forAscendingOrderedField("uuid")
            .withContent(
                List.of(
                    DigitalObject.builder()
                        .created("2020-10-14T00:00:00")
                        .lastModified("2020-10-14T00:00:00")
                        .uuid("dde78cea-985b-4863-a782-1233978db71a")
                        .label(Locale.GERMAN, "Testdigitalisat")
                        .identifier(
                            Identifier.builder()
                                .namespace("mdz-obj")
                                .id("bsb12345678")
                                .uuid("73724e05-4972-436c-a8ba-79240f675b46")
                                .build())
                        .refId(529)
                        .build()))
            .build();

    when(collectionService.findDigitalObjects(any(Collection.class), any(PageRequest.class)))
        .thenReturn(expected);

    testJson(path, "/v5/collections/900fbf70-12de-4483-a0e3-9d77ea49626e_digitalobjects.json");
  }

  @DisplayName("shall return a paged list of subcollections for a collection")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v5/collections/900fbf70-12de-4483-a0e3-9d77ea49626e/subcollections?pageSize=1&pageNumber=0"
      })
  void testFindSubcollections(String path) throws Exception {
    PageResponse<Collection> expected =
        PageResponse.builder()
            .forRequestPage(0)
            .forPageSize(1)
            .withTotalElements(156)
            .forDescendingOrderedField("lastModified")
            .forAscendingOrderedField("uuid")
            .withContent(
                List.of(
                    Collection.builder()
                        .created("2022-03-31T11:32:04.189939")
                        .lastModified("2022-05-02T16:18:52.710802")
                        .uuid("900fbf70-12de-4483-a0e3-9d77ea49626e")
                        .label(Locale.GERMAN, "Test-Sammlung")
                        .publicationStart("2022-05-02")
                        .build()))
            .build();

    when(collectionService.findChildren(any(Collection.class), any(PageRequest.class)))
        .thenReturn(expected);

    testJson(path, "/v5/collections/find_with_result.json");
  }

  @DisplayName("shall return a paged list of top collections")
  @ParameterizedTest
  @ValueSource(strings = {"/v5/collections/top?pageSize=1&pageNumber=0"})
  void testFindTopCollections(String path) throws Exception {
    PageResponse<Collection> expected =
        PageResponse.builder()
            .forRequestPage(0)
            .forPageSize(1)
            .withTotalElements(156)
            .forDescendingOrderedField("lastModified")
            .forAscendingOrderedField("uuid")
            .withContent(
                List.of(
                    Collection.builder()
                        .created("2022-03-31T11:32:04.189939")
                        .lastModified("2022-05-02T16:18:52.710802")
                        .uuid("900fbf70-12de-4483-a0e3-9d77ea49626e")
                        .label(Locale.GERMAN, "Test-Sammlung")
                        .publicationStart("2022-05-02")
                        .build()))
            .build();

    when(collectionService.findRootNodes(any(PageRequest.class))).thenReturn(expected);

    testJson(path, "/v5/collections/find_with_result.json");
  }
}
