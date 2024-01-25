package de.digitalcollections.model.jackson.list;

import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import de.digitalcollections.model.list.ListRequest;
import de.digitalcollections.model.list.ListResponse;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.FilterOperation;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.security.User;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ListResponseTest extends BaseJsonSerializationTest {

  private ListResponse<User, ListRequest> createObject() {
    ListRequest listRequest = new ListRequest();
    // filtering
    FilterCriterion filterCriteria1 =
        new FilterCriterion("longField", FilterOperation.EQUALS, 5L, null, null, null);
    FilterCriterion filterCriteria2 =
        new FilterCriterion(
            "dateField",
            FilterOperation.BETWEEN,
            null,
            LocalDate.parse("2020-01-01"),
            LocalDate.parse("2020-01-31"),
            null);
    Filtering filtering = Filtering.builder().add(filterCriteria1).add(filterCriteria2).build();
    listRequest.setFiltering(filtering);

    List<User> content = new ArrayList<>();
    User user = new User();
    user.setEmail("test@user.de");
    content.add(user);

    ListResponse listResponse = new ListResponse(content, listRequest);
    return listResponse;
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    ListResponse<User, ListRequest> listResponse = createObject();
    checkSerializeDeserialize(listResponse, "serializedTestObjects/list/ListResponse.json");
  }
}
