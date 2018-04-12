package de.digitalcollections.cudami.model.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.model.api.identifiable.parts.Text;
import de.digitalcollections.cudami.model.api.identifiable.resource.ResourceType;
import de.digitalcollections.cudami.model.api.identifiable.resource.Webpage;
import de.digitalcollections.cudami.model.api.security.User;
import de.digitalcollections.cudami.model.impl.identifiable.entity.WebsiteImpl;
import de.digitalcollections.cudami.model.impl.identifiable.parts.TextImpl;
import de.digitalcollections.cudami.model.impl.identifiable.resource.WebpageImpl;
import de.digitalcollections.cudami.model.impl.security.UserImpl;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Set;
import org.apache.commons.beanutils.BeanUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Ignore
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

  @Test
  public void testWebsite() throws Exception {
    WebsiteImpl website = new WebsiteImpl(new URL("http://www.example.org/"));
    String serializedObject = mapper.writeValueAsString(website);
    checkSerializeDeserialize(website);
  }

  @Test
  public void testWebpage() throws Exception {
    Text text = new TextImpl();
    text.setText(
        "{\"type\":\"doc\",\"content\":[{\"type\":\"heading\",\"attrs\":{\"level\":3},\"content\":[{\"type\":\"text\",\"text\":\"Impressum\"}]},{\"type\":\"heading\",\"attrs\":{\"level\":4},\"content\":[{\"type\":\"text\",\"text\":\"Bayerische Staatsbibliothek\"}]},{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"Ludwigstraße 16\"},{\"type\":\"hard_break\"},{\"type\":\"text\",\"text\":\"80539 München\"}]},{\"type\":\"heading\",\"attrs\":{\"level\":4},\"content\":[{\"type\":\"text\",\"text\":\"Gesetzlicher Vertreter:\"}]},{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"Generaldirektor Dr. Klaus Ceynowa\"}]},{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"marks\":[{\"type\":\"strong\"}],\"text\":\"Telefon:\"},{\"type\":\"text\",\"text\":\" +49 89 28638-0\"},{\"type\":\"hard_break\"},{\"type\":\"text\",\"marks\":[{\"type\":\"strong\"}],\"text\":\"Fax:\"},{\"type\":\"text\",\"text\":\" +49 89 28638-2200\"},{\"type\":\"hard_break\"},{\"type\":\"text\",\"marks\":[{\"type\":\"strong\"}],\"text\":\"E-Mail:\"},{\"type\":\"text\",\"text\":\" direktion [AT] bsb-muenchen.de\"},{\"type\":\"hard_break\"},{\"type\":\"text\",\"marks\":[{\"type\":\"strong\"}],\"text\":\"Internet:\"},{\"type\":\"text\",\"text\":\" \"},{\"type\":\"text\",\"marks\":[{\"type\":\"link\",\"attrs\":{\"href\":\"https://www.bsb-muenchen.de\",\"title\":null}}],\"text\":\"https://www.bsb-muenchen.de\"},{\"type\":\"hard_break\"},{\"type\":\"text\",\"marks\":[{\"type\":\"strong\"}],\"text\":\"Umsatzsteueridentifikationsnummer:\"},{\"type\":\"text\",\"text\":\" DE 811335517\"}]},{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"Die Bayerische Staatsbibliothek ist eine dem Bayerischen Staatsministerium für Bildung und Kultus, Wissenschaft und Kunst nachgeordnete Behörde der Mittelstufe mit dem Sitz in München.\"},{\"type\":\"hard_break\"},{\"type\":\"text\",\"marks\":[{\"type\":\"link\",\"attrs\":{\"href\":\"https://www.km.bayern.de/\",\"title\":null}}],\"text\":\"Bayerischen Staatsministerium für Bildung und Kultus, Wissenschaft und Kunst\"}]},{\"type\":\"bullet_list\",\"content\":[{\"type\":\"list_item\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"test 1\"}]}]},{\"type\":\"list_item\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"test 2\"}]}]},{\"type\":\"list_item\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"test 3\"}]}]}]},{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"Mehr Text.\"}]},{\"type\":\"ordered_list\",\"attrs\":{\"order\":1},\"content\":[{\"type\":\"list_item\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"test 1\"}]}]},{\"type\":\"list_item\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"test 2\"}]}]},{\"type\":\"list_item\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"test 3\"}]}]}]},{\n"
            + "  \"type\": \"embedded_code_block\",\n"
            + "  \"content\": [\n"
            + "    {\n"
            + "     \"type\": \"text\",\n"
            + "     \"text\": \"<iframe style=\\\"border: 1px solid lightgrey\\\" frameborder=\\\"no\\\" width=\\\"98%\\\" height=\\\"auto\\\" src=\\\"https://statistiken.digitale-sammlungen.de/index.php?module=CoreAdminHome&amp;action=optOut&amp;language=de\\\"></iframe>\"\n"
            + "    }\n"
            + "  ]\n"
            + "}]}"
    );
    Webpage webpage = new WebpageImpl();
    webpage.setContentBlocks(text);
    webpage.setResourceType(ResourceType.WEBPAGE);

    checkSerializeDeserialize(webpage);

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
