package de.digitalcollections.model.jackson.mixin.list;

import de.digitalcollections.model.identifiable.semantic.Subject;
import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import de.digitalcollections.model.list.ListRequest;
import de.digitalcollections.model.list.ListResponse;
import de.digitalcollections.model.semantic.Tag;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The ListResponseMixIn")
class ListResponseMixInTest extends BaseJsonSerializationTest {

  @DisplayName("can handle a subject")
  @Test
  public void testSubject() throws Exception {
    Subject subject = Subject.builder().subjectType("foo").build();
    ListResponse<Subject, ListRequest> listResponse =
        new ListResponse(List.of(subject), new ListRequest());
    checkSerializeDeserialize(listResponse, "serializedTestObjects/list/ListResponse_Subject.json");
  }

  @DisplayName("can handle a tag")
  @Test
  public void testTag() throws Exception {
    Tag tag = Tag.builder().value("foo").build();
    ListResponse<Tag, ListRequest> listResponse = new ListResponse(List.of(tag), new ListRequest());
    checkSerializeDeserialize(listResponse, "serializedTestObjects/list/ListResponse_Tag.json");
  }
}
