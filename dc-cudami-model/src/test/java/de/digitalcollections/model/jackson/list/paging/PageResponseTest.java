package de.digitalcollections.model.jackson.list.paging;

import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.FilterOperation;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.security.User;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class PageResponseTest extends BaseJsonSerializationTest {

  private PageResponse<User> createObject() {
    List<User> content = new ArrayList<>(0);
    User user = new User();
    user.setEmail("test@user.de");
    content.add(user);
    PageResponse pageResponse = new PageResponse(content);

    // paging
    PageRequest pageRequest = new PageRequest(3, 15);

    // searching
    pageRequest.setSearchTerm("Hello");

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
    pageRequest.setFiltering(filtering);

    pageResponse.setRequest(pageRequest);
    pageResponse.setExecutedSearchTerm("Hello");

    return pageResponse;
  }

  @Test
  @DisplayName("can serialize and deserialize different types")
  public void differentTypes() throws Exception {
    List<Identifiable> content = new ArrayList<>();
    DigitalObject digitalObject = new DigitalObject();
    digitalObject.setLabel("DigitalObject-Label");
    content.add(digitalObject);
    Webpage webpage = new Webpage();
    webpage.setLabel("Webpage-Label");
    content.add(webpage);

    PageRequest pageRequest = new PageRequest("Label", 3, 15, null);
    PageResponse<Identifiable> resp = new PageResponse<>();
    resp.setRequest(pageRequest);
    resp.setContent(content);
    resp.setTotalElements(2);
    resp.setExecutedSearchTerm("Label");

    checkSerializeDeserialize(
        resp, "serializedTestObjects/list/paging/PageResponse_differentTypes.json");
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    PageResponse<User> pageResponse = createObject();
    checkSerializeDeserialize(pageResponse, "serializedTestObjects/list/paging/PageResponse.json");
    // TODO try to eliminate "className" : "de.digitalcollections.model.security.User" from
    // serialization. seems complicated:
    // https://stackoverflow.com/questions/34193177/why-does-jackson-polymorphic-serialization-not-work-in-lists
  }
}
