package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.DigitalObjectService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.cudami.server.model.DigitalObjectBuilder;
import de.digitalcollections.cudami.server.model.PageResponseBuilder;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
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
        "/latest/digitalobjects/51f9e6c9-4c91-4fdc-9563-26c17ff110cc/collections?active=true&pageNumber=0&pageSize=1000"
      })
  public void emptyCollectionsForDigitalObject(String path) throws Exception {

    PageResponse<Collection> expected =
        new PageResponseBuilder(Collection.class)
            .withoutContent()
            .forRequestPage(0)
            .forPageSize(1000)
            .forStartDate("c.publication_start", "2021-03-31")
            .forEndDate("c.publication_end", "2021-03-31")
            .build();

    DigitalObject digitalObject = new DigitalObjectBuilder().atPath(path).build();
    when(digitalObjectService.getActiveCollections(eq(digitalObject), any(PageRequest.class)))
        .thenReturn(expected);

    testJson(path);
  }
}
