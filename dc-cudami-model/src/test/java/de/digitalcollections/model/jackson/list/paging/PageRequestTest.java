package de.digitalcollections.model.jackson.list.paging;

import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import de.digitalcollections.model.list.paging.PageRequest;
import org.junit.jupiter.api.Test;

public class PageRequestTest extends BaseJsonSerializationTest {

  private PageRequest createObject() {
    PageRequest pageRequest = new PageRequest("hallo", 3, 15, null);
    return pageRequest;
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    PageRequest pageRequest = createObject();
    checkSerializeDeserialize(pageRequest, "serializedTestObjects/list/paging/PageRequest.json");
  }
}
