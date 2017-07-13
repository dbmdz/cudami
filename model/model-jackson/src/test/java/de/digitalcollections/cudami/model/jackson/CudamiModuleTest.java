package de.digitalcollections.cudami.model.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.model.api.security.User;
import de.digitalcollections.cudami.model.impl.security.UserImpl;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import org.apache.commons.beanutils.BeanUtils;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CudamiModuleTest {

  ObjectMapper mapper;

  @Before
  public void setUp() {
    mapper = new ObjectMapper();
    mapper.registerModule(new CudamiModule());
  }

  @Test
  public void testSerializeDeserializeUser() throws Exception {
    User user = new UserImpl();

    checkSerializeDeserialize(user);

  }

//  @Test
//  public void testSerializeDeserializeContentInDefaultLocale() throws Exception {
//    Text content = new TextImpl("de", "test");
//
//    checkSerializeDeserialize(content);
//  }
//
//  @Test
//  public void testSerializeDeserializeEmptyContent() throws Exception {
//    Text content = new TextImpl();
//
//    checkSerializeDeserialize(content);
//  }
//
//  @Test
//  public void testSerializeDeserializeContentInSeveralLanguages() throws Exception {
//    Text content = new TextImpl("de", "test");
//    content.setText("it", "testo");
//
//    checkSerializeDeserialize(content);
//  }
  // -------------------------------------------------------------------------------------------------------
  private <T> void checkSerializeDeserialize(T objectIn) throws Exception {
    T objectOut = (T) serializeDeserialize(objectIn);

    try {
      Set<String> keys = BeanUtils.describe(objectIn).keySet();
      for (String key : keys) {
        if ("UUID".equals(key)) {
          BeanUtils.setProperty(objectIn, key, null);
          BeanUtils.setProperty(objectOut, key, null);
        } else {
          BeanUtils.setProperty(objectIn, key + ".UUID", null);
          BeanUtils.setProperty(objectOut, key + ".UUID", null);
        }
      }
    } catch (InvocationTargetException e) {
      System.out.println(e);
    }

    /*
     * try { Method methodGetUuid = objectIn.getClass().getMethod("getUUID"); UUID uuid = (UUID)
     * methodGetUuid.invoke(objectIn); Method methodSetUUid = objectOut.getClass().getMethod("setUUID", UUID.class);
     * methodSetUUid.invoke(objectOut, uuid); } catch (NoSuchMethodException ignore) { }
     */
    try {
      assertThat(objectOut).isEqualToComparingFieldByFieldRecursively(objectIn);
      // System.out.println("IN=" + dump(objectIn) + "\nOUT=" + dump(objectOut) + "\n\n");
    } catch (Throwable e) {
      System.err.println("ERR: IN=" + dump(objectIn) + "\nOUT=" + dump(objectOut) + "\n\nERROR=" + e.getClass() + "=" + e.getMessage());
      throw e;
    }
  }

  private Object serializeDeserialize(Object o) throws JsonProcessingException, IOException {
    String serializedObject = mapper.writeValueAsString(o);
    Class valueType = o.getClass();
    Object deserializedObject = mapper.readValue(serializedObject, valueType);
    return deserializedObject;
  }

  private String dump(Object o) throws JsonProcessingException {
    return mapper.writeValueAsString(o);
  }

}
