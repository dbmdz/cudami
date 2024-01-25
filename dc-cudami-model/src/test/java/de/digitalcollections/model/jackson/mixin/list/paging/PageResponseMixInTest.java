package de.digitalcollections.model.jackson.mixin.list.paging;

import de.digitalcollections.model.identifiable.semantic.Subject;
import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.semantic.Tag;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The PageResponseMixIn")
class PageResponseMixInTest extends BaseJsonSerializationTest {

  @DisplayName("can handle a subject")
  @Test
  public void testSubject() throws Exception {
    Subject subject = Subject.builder().subjectType("foo").build();
    PageResponse<Subject> pageResponse =
        PageResponse.builder().withContent(List.of(subject)).build();
    checkSerializeDeserialize(
        pageResponse, "serializedTestObjects/list/paging/PageResponse_Subject.json");
  }

  @DisplayName("can handle a tag")
  @Test
  public void testTag() throws Exception {
    Tag tag = Tag.builder().value("foo").build();
    PageResponse<Tag> pageResponse = PageResponse.builder().withContent(List.of(tag)).build();
    checkSerializeDeserialize(
        pageResponse, "serializedTestObjects/list/paging/PageResponse_Tag.json");
  }
}
