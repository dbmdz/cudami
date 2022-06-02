package de.digitalcollections.cudami.server.controller.legacy;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.NullHandling;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The V5 migration helper")
class V5MigrationHelperTest {

  private ObjectMapper objectMapper;

  @BeforeEach
  public void beforeEach() {
    objectMapper = new DigitalCollectionsObjectMapper();
  }

  @DisplayName("does not modify a null pageresponse")
  @Test
  public void nullPageResult() throws JsonProcessingException {
    String actual = V5MigrationHelper.migrate(null, objectMapper);
    assertThat(actual).isNull();
  }

  @DisplayName("can work with an empty pageresult")
  @Test
  public void emptyPageResponse() throws JsonProcessingException {
    PageResponse pageResponse = PageResponse.builder().withoutContent().build();

    String actual = V5MigrationHelper.migrate(pageResponse, objectMapper);
    assertThat(actual).isNotNull();
    assertThat(actual).contains("pageRequest");
  }

  @DisplayName("renames executedSearchTerm to query")
  @Test
  public void renamesExecutedSearchTerm() throws JsonProcessingException {
    PageResponse pageResponse = PageResponse.builder().withoutContent().build();
    PageRequest pageRequest = PageRequest.builder().pageSize(1).pageNumber(0).build();
    pageResponse.setRequest(pageRequest);
    pageResponse.setExecutedSearchTerm("hugo");

    String actual = V5MigrationHelper.migrate(pageResponse, objectMapper);
    assertThat(actual).contains("query\":\"hugo");
    assertThat(actual).doesNotContain("executedSearchTerm");
  }

  @DisplayName("renames searchTerm to query")
  @Test
  public void renameSearchTerm() throws JsonProcessingException {
    PageResponse pageResponse = PageResponse.builder().withoutContent().build();
    PageRequest pageRequest =
        PageRequest.builder().searchTerm("blubb").pageSize(1).pageNumber(0).build();
    pageResponse.setRequest(pageRequest);

    String actual = V5MigrationHelper.migrate(pageResponse, objectMapper);
    assertThat(actual).contains("query\":\"blubb");
    assertThat(actual).doesNotContain("searchTerm");
  }

  @DisplayName("renames both, searchTerm and executedSearchTerm to query")
  @Test
  public void renameSearchTermAndExecutedSearchTerm() throws JsonProcessingException {
    PageResponse pageResponse = PageResponse.builder().withoutContent().build();
    PageRequest pageRequest =
        PageRequest.builder().searchTerm("blubb").pageSize(1).pageNumber(0).build();
    pageResponse.setRequest(pageRequest);
    pageResponse.setExecutedSearchTerm("hugo");

    String actual = V5MigrationHelper.migrate(pageResponse, objectMapper);
    assertThat(actual).contains("query\":\"blubb");
    assertThat(actual).contains("query\":\"hugo");
    assertThat(actual).doesNotContain("searchTerm");
    assertThat(actual).doesNotContain("executedSearchTerm");
  }

  @DisplayName("sets the ascending and descending fields on a single order field")
  @Test
  public void setAscendingAndDescending() throws JsonProcessingException {
    PageRequest pageRequest =
        PageRequest.builder()
            .pageSize(1)
            .pageNumber(0)
            .sorting(
                Sorting.builder().order(Order.builder().direction(Direction.ASC).build()).build())
            .build();
    PageResponse pageResponse = PageResponse.builder().withoutContent().build();
    pageResponse.setRequest(pageRequest);

    String actual = V5MigrationHelper.migrate(pageResponse, objectMapper);
    assertThat(actual).contains("ascending\":true");
    assertThat(actual).contains("descending\":false");
  }

  @DisplayName("sets the ascending and descending fields on multiple order fields")
  @Test
  public void setAscendingAndDescendingMultiple() throws JsonProcessingException {
    PageRequest pageRequest =
        PageRequest.builder()
            .pageSize(1)
            .pageNumber(0)
            .sorting(
                Sorting.builder()
                    .order(Order.builder().property("foo").direction(Direction.ASC).build())
                    .order(Order.builder().property("bar").direction(Direction.DESC).build())
                    .build())
            .build();
    PageResponse pageResponse = PageResponse.builder().withoutContent().build();
    pageResponse.setRequest(pageRequest);

    String actual = V5MigrationHelper.migrate(pageResponse, objectMapper);
    assertThat(actual).contains("ascending\":true");
    assertThat(actual).contains("descending\":false");
    assertThat(actual).contains("ascending\":false");
    assertThat(actual).contains("descending\":true");
  }

  // TODO: NullHandling output

  @Test
  public void testMigrateOrderList() {
    List<Order> list =
        List.of(
            new Order(Direction.DESC, false, NullHandling.NULLS_LAST, "prop1"),
            new Order(Direction.DESC, true, NullHandling.NULLS_LAST, "prop1"));
    List<Order> migratedList = V5MigrationHelper.migrate(list);
    for (Order order : migratedList) {
      assertThat(order.isIgnoreCase()).isTrue();
    }
  }
}
