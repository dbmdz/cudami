package de.digitalcollections.cudami.model.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.model.api.security.User;
import de.digitalcollections.cudami.model.impl.identifiable.entity.WebsiteImpl;
import de.digitalcollections.cudami.model.impl.security.UserImpl;
import de.digitalcollections.prosemirror.model.jackson.ProseMirrorObjectMapper;
import java.net.URL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CudamiModuleTest extends BaseSerializationTest {

  ObjectMapper mapper;

  @BeforeEach
  public void setUp() {
    mapper = new ObjectMapper();
    mapper.registerModule(new CudamiModule());
    ProseMirrorObjectMapper.customize(mapper);
  }

  @Override
  protected ObjectMapper getMapper() {
    return mapper;
  }

  @Test
  public void testSerializeDeserializeUser() throws Exception {
    User user = new UserImpl();
    checkSerializeDeserialize(user);
  }

  @Test
  public void testWebsite() throws Exception {
    WebsiteImpl website = new WebsiteImpl(new URL("http://www.example.org/"));
    String serializedObject = mapper.writeValueAsString(website);
    checkSerializeDeserialize(website);
  }
}
