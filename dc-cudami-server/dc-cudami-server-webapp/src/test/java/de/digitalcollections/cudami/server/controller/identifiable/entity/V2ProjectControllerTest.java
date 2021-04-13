package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.ProjectService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.IdentifiableType;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.EntityType;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.paging.Direction;
import de.digitalcollections.model.paging.NullHandling;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.OrderBuilder;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.paging.Sorting;
import de.digitalcollections.model.paging.SortingBuilder;
import de.digitalcollections.model.text.LocalizedText;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(V2ProjectController.class)
@DisplayName("The V2 ProjectController")
public class V2ProjectControllerTest extends BaseControllerTest {

  @MockBean private ProjectService projectService;

  @DisplayName("shall return a pages project list")
  @ParameterizedTest
  @ValueSource(strings = {"/v2/projects/?pageNumber=0&pageSize=1"})
  public void projectList(String path) throws Exception {
    SearchPageResponse<Project> response = new SearchPageResponse();
    response.setTotalElements(395);

    Project project = new Project();
    project.setCreated(LocalDateTime.parse("2020-09-30T16:25:10.609465"));
    Identifier identifier =
        new Identifier(
            UUID.fromString("ae2a0a61-5255-46d4-8acf-cfddd3527338"), "mdz-proj", "1467037957");
    identifier.setUuid(UUID.fromString("898947e9-0d61-4572-b87e-05a01868001d"));
    project.setIdentifiers(Set.of(identifier));
    project.setLabel(new LocalizedText(Locale.GERMAN, "100(0) Dokumente"));
    project.setLastModified(LocalDateTime.parse("2021-04-13T04:15:01.274821"));
    project.setType(IdentifiableType.ENTITY);
    project.setUuid(UUID.fromString("ae2a0a61-5255-46d4-8acf-cfddd3527338"));
    project.setEntityType(EntityType.PROJECT);
    project.setRefId(1300623);

    response.setContent(List.of(project));
    Order order1 =
        new OrderBuilder()
            .direction(Direction.ASC)
            .ignoreCase(false)
            .nullHandling(NullHandling.NATIVE)
            .property("label")
            .subProperty("de")
            .build();
    Order order2 =
        new OrderBuilder()
            .direction(Direction.ASC)
            .ignoreCase(false)
            .nullHandling(NullHandling.NATIVE)
            .property("label")
            .subProperty("")
            .build();
    Sorting sorting = new SortingBuilder().order(order1).order(order2).build();
    SearchPageRequest pageRequest = new SearchPageRequest();
    pageRequest.setPageSize(1);
    pageRequest.setPageNumber(0);
    pageRequest.setSorting(sorting);
    response.setPageRequest(pageRequest);

    when(projectService.find(any(SearchPageRequest.class))).thenReturn(response);

    testJson(path, "/v2/projects/projects.json");
  }
}
