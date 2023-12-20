package de.digitalcollections.model.jackson.list;

import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import de.digitalcollections.model.list.ListRequest;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import org.junit.jupiter.api.Test;

public class ListRequestTest extends BaseJsonSerializationTest {

  private ListRequest createObject() {
    ListRequest listRequest = new ListRequest();
    listRequest.add(
        Filtering.builder()
            .add(FilterCriterion.builder().withExpression("label").startsWith("A").build())
            .build());
    listRequest.add(
        Sorting.builder()
            .order(Order.builder().direction(Direction.ASC).property("label").build())
            .build());
    listRequest.setSearchTerm("hello");
    return listRequest;
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    ListRequest listRequest = createObject();
    checkSerializeDeserialize(listRequest, "serializedTestObjects/list/ListRequest.json");
  }
}
