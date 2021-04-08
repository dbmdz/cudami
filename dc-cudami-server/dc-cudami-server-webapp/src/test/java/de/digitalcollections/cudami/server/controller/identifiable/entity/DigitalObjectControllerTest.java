package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.DigitalObjectService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.filter.FilterCriterion;
import de.digitalcollections.model.filter.FilterOperation;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import java.time.LocalDate;
import java.util.List;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(DigitalObjectController.class)
@DisplayName("The DigitalObjectController")
class DigitalObjectControllerTest extends BaseControllerTest {

  @MockBean private DigitalObjectService digitalObjectService;

  @DisplayName("can return empty collections for a digital object")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/latest/digitalobjects/51f9e6c9-4c91-4fdc-9563-26c17ff110cc/collections?active=true&pageNumber=0&pageSize=1000",
        "/v3/digitalobjects/51f9e6c9-4c91-4fdc-9563-26c17ff110cc/collections?active=true&pageNumber=0&pageSize=1000"
      })
  public void emptyCollectionsForDigitalObject(String path) throws Exception {
    DigitalObject digitalObject = new DigitalObject();
    digitalObject.setUuid(extractFirstUuidFromPath(path));
    PageResponse<Collection> emptyPageResponse = new PageResponse<>();
    PageRequest pageRequest = new PageRequest();
    Filtering filtering = new Filtering();
    FilterCriterion filterCriterionStart =
        new FilterCriterion(
            "c.publication_start",
            FilterOperation.LESS_THAN_OR_EQUAL_TO_AND_SET,
            LocalDate.parse("2021-03-31"));
    FilterCriterion filterCriterionEnd =
        new FilterCriterion(
            "c.publication_end",
            FilterOperation.GREATER_THAN_OR_NOT_SET,
            LocalDate.parse("2021-03-31"));
    filtering.setFilterCriteria(List.of(filterCriterionStart, filterCriterionEnd));
    pageRequest.setFiltering(filtering);
    pageRequest.setPageNumber(0);
    pageRequest.setPageSize(1000);
    emptyPageResponse.setPageRequest(pageRequest);

    when(digitalObjectService.getActiveCollections(eq(digitalObject), any(PageRequest.class)))
        .thenReturn(emptyPageResponse);

    mockMvc
        .perform(get(path))
        .andExpect(status().isOk())
        .andExpect(content().contentType(ContentType.APPLICATION_JSON.getMimeType()))
        .andExpect(content().json(getJsonFromFileResource(path)));
  }
}
