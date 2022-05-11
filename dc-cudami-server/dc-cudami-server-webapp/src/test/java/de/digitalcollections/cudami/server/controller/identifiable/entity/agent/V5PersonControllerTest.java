package de.digitalcollections.cudami.server.controller.identifiable.entity.agent;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.agent.PersonService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(V5PersonController.class)
@DisplayName("The V5 PersonController")
class V5PersonControllerTest extends BaseControllerTest {

  @MockBean private PersonService personService;

  @DisplayName("shall return a paged list of persons")
  @ParameterizedTest
  @ValueSource(strings = {"/v5/persons?pageSize=1&pageNumber=0"})
  void testFind(String path) throws Exception {
    PageResponse<Person> expected =
        (PageResponse<Person>)
            PageResponse.builder()
                .forRequestPage(0)
                .forPageSize(1)
                .withTotalElements(75)
                .forAscendingOrderedField("label", "de")
                .forAscendingOrderedField("label")
                .build();

    when(personService.find(any(PageRequest.class))).thenReturn(expected);

    testJson(path, "/v5/persons/find_with_empty_result.json");
  }
}
