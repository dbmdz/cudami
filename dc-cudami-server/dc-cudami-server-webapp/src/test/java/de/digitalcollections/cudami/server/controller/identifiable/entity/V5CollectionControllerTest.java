package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.CollectionService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
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
  @ValueSource(strings = {"/v5/collections/search?pageSize=1&pageNumber=0"})
  void testFind(String path) throws Exception {
    PageResponse<Collection> expected =
        (PageResponse<Collection>)
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

    when(collectionService.find(any(PageRequest.class))).thenReturn(expected);

    testJson(path, "/v5/collections/find_with_result.json");
  }

  @DisplayName("shall return a paged list of digital objects for a collections")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v5/collections/900fbf70-12de-4483-a0e3-9d77ea49626e/digitalobjects?pageSize=1&pageNumber=0"
      })
  void testFindDigitalobjects(String path) throws Exception {

    // FIXME ab hier gehts weiter

    PageResponse<Collection> expected =
        (PageResponse<Collection>)
            PageResponse.builder()
                .forRequestPage(0)
                .forPageSize(1)
                .withTotalElements(156)
                .forDescendingOrderedField("lastModified")
                .forAscendingOrderedField("uuid")
                .withContent(
                    List.of(
                        DigitalObject.builder()
                            .created("2022-03-31T11:32:04.189939")
                            .lastModified("2022-05-02T16:18:52.710802")
                            .uuid("900fbf70-12de-4483-a0e3-9d77ea49626e")
                            .label(Locale.GERMAN, "Test-Sammlung")
                            .build()))
                .build();

    when(collectionService.find(any(PageRequest.class))).thenReturn(expected);

    testJson(path, "/v5/collections//900fbf70-12de-4483-a0e3-9d77ea49626e_digitalobjects.json");
  }
}
