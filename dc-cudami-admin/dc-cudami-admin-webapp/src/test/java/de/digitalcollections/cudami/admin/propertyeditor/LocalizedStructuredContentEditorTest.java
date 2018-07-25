package de.digitalcollections.cudami.admin.propertyeditor;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.admin.config.SpringConfigBusinessForTest;
import de.digitalcollections.cudami.admin.config.SpringConfigWeb;
import de.digitalcollections.model.api.identifiable.parts.structuredcontent.LocalizedStructuredContent;
import de.digitalcollections.model.impl.identifiable.parts.structuredcontent.LocalizedStructuredContentImpl;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SpringConfigWeb.class, SpringConfigBusinessForTest.class})
public class LocalizedStructuredContentEditorTest implements InitializingBean {

  @Autowired
  private ObjectMapper objectMapper;

  private LocalizedStructuredContentEditor documentEditor;

  @Override
  public void afterPropertiesSet() throws Exception {
    DigitalCollectionsObjectMapper.customize(objectMapper);
    this.documentEditor = new LocalizedStructuredContentEditor();
    this.documentEditor.setObjectMapper(objectMapper);
  }

  private LocalizedStructuredContent createLocalizedStructuredContent(String filename) {
    try {
      String json = createJson(filename);
      LocalizedStructuredContent lsc = objectMapper.readValue(json, LocalizedStructuredContent.class);
      return lsc;
    } catch (IOException ex) {
      return null;
    }
  }

  private String createJson(String filename) {
    try {
      String json = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(filename), StandardCharsets.UTF_8);
      return json;
    } catch (IOException ex) {
      return null;
    }
  }

  /**
   * Test of getAsText method, of class LocalizedStructuredContentEditor.
   */
  @Test
  public void testGetAsText() {
    String expResult = createJson("localizedStructuredContent.json").replaceAll("\\s", "");
    LocalizedStructuredContent lsc = createLocalizedStructuredContent("localizedStructuredContent.json");
    documentEditor.setValue(lsc);

    String result = documentEditor.getAsText().replaceAll("\\s", "");
    assertEquals(expResult, result);
  }

  /**
   * Test of setAsText method, of class LocalizedStructuredContentEditor.
   */
  @Test
  public void testSetAsText() {
    LocalizedStructuredContent expResult = createLocalizedStructuredContent("localizedStructuredContent.json");
    String json = createJson("localizedStructuredContent.json");
    documentEditor.setAsText(json);

    LocalizedStructuredContentImpl result = (LocalizedStructuredContentImpl) documentEditor.getValue();
    assertEquals(expResult.getLocalizedStructuredContent().size(), result.getLocalizedStructuredContent().size());
  }

}
