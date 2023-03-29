package de.digitalcollections.cudami.server.controller.identifiable.entity.relation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.relation.EntityRelationService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(V5EntityRelationController.class)
@DisplayName("The V5 EntityRelationController")
class V5EntityRelationControllerTest extends BaseControllerTest {

  @MockBean private EntityRelationService entityRelationService;

  @DisplayName("shall return a list of related entities with a predicate")
  @ParameterizedTest
  @ValueSource(strings = {"/v5/entities/relations?predicate=is_part_of&pageNumber=0&pageSize=1"})
  public void listOfRelatedEntities(String path) throws Exception {
    PageResponse<EntityRelation> expected =
        (PageResponse<EntityRelation>)
            PageResponse.builder()
                .forRequestPage(0)
                .forPageSize(1)
                .forEqualPredicate("predicate", "is_part_of")
                .withTotalElements(109)
                .withoutContent()
                .build();

    when(entityRelationService.find(any(PageRequest.class))).thenReturn(expected);

    testJson(path, "/v5/entities/find_with_empty_result.json");
  }
}
