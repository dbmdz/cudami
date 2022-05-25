package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.EntityService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.relation.EntityRelationService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.IdentifiableObjectType;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(V5EntityController.class)
@DisplayName("The V5 EntityController")
class V5EntityControllerTest extends BaseControllerTest {

  @MockBean(name = "entityService")
  private EntityService<Entity> entityService;

  @MockBean private EntityRelationService entityRelationService;

  @DisplayName("shall return a paged list of all entities")
  @ParameterizedTest
  @ValueSource(strings = {"/v5/entities?pageSize=1&pageNumber=0"})
  void testFindAll(String path) throws Exception {
    // We must modify the Entity, which the EntityBuilder builds, because
    // we need to have the entityType set to Webpage (which is currently
    // not possible for the generic EntityBuilder
    Entity entity =
        Entity.builder()
            .created("2018-05-02T00:00:00")
            .lastModified("2018-05-02T00:00:00")
            .description(Locale.GERMAN, "")
            .label(Locale.GERMAN, "Test")
            .refId(71)
            .uuid("e91464a1-588b-434b-a88e-b6a1c3824c85")
            .identifiableObjectType(IdentifiableObjectType.WEBSITE)
            .build();

    PageResponse<Entity> expected =
        (PageResponse<Entity>)
            PageResponse.builder()
                .forRequestPage(0)
                .forPageSize(1)
                .withTotalElements(1366380)
                .forAscendingOrderedField("label", "de")
                .forAscendingOrderedField("label")
                .forAscendingOrderedField("uuid")
                .withContent(List.of(entity))
                .build();

    when(entityService.find(any(PageRequest.class))).thenReturn(expected);

    testJson(path, "/v5/entities/find_with_result.json");
  }

  @DisplayName("shall return a paged list of all entities from the search endpoint")
  @ParameterizedTest
  @ValueSource(strings = {"/v5/entities/search?pageSize=1&pageNumber=0"})
  void testFind(String path) throws Exception {
    // We must modify the Entity, which the EntityBuilder builds, because
    // we need to have the entityType set to Webpage (which is currently
    // not possible for the generic EntityBuilder
    Entity entity =
        Entity.builder()
            .created("2018-05-02T00:00:00")
            .lastModified("2018-05-02T00:00:00")
            .description(Locale.GERMAN, "")
            .label(Locale.GERMAN, "Test")
            .refId(71)
            .uuid("e91464a1-588b-434b-a88e-b6a1c3824c85")
            .identifiableObjectType(IdentifiableObjectType.WEBSITE)
            .build();

    PageResponse<Entity> expected =
        (PageResponse<Entity>)
            PageResponse.builder()
                .forRequestPage(0)
                .forPageSize(1)
                .withTotalElements(1366380)
                .forAscendingOrderedField("label", "de")
                .forAscendingOrderedField("label")
                .forAscendingOrderedField("uuid")
                .withContent(List.of(entity))
                .build();

    when(entityService.find(any(PageRequest.class))).thenReturn(expected);

    testJson(path, "/v5/entities/find_with_result.json");
  }
}
