package de.digitalcollections.model.jackson.security;

import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import de.digitalcollections.model.security.User;
import org.junit.jupiter.api.Test;

public class UserTest extends BaseJsonSerializationTest {

  public User createObject() {
    User user = new User();
    user.setEmail("test1@user.de");
    user.setPasswordHash("$2a$10$bSUNjxCeQiipFl/QhzeckLGCAOyQLgFs5teAVruvSkL3.tdGdO");
    return user;
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    User user = createObject();
    checkSerializeDeserialize(user);
  }
}
