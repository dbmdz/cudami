package de.digitalcollections.cudami.admin.propertyeditor;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.admin.config.SpringConfigBusinessForTest;
import de.digitalcollections.cudami.admin.config.SpringConfigWeb;
import de.digitalcollections.cudami.model.api.identifiable.parts.MultilanguageDocument;
import de.digitalcollections.cudami.model.impl.identifiable.parts.MultilanguageDocumentImpl;
import de.digitalcollections.cudami.model.jackson.CudamiObjectMapper;
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
public class MultilanguageDocumentEditorTest implements InitializingBean {

  @Autowired
  private ObjectMapper objectMapper;

  private MultilanguageDocumentEditor documentEditor;

  @Override
  public void afterPropertiesSet() throws Exception {
    CudamiObjectMapper.customize(objectMapper);
    this.documentEditor = new MultilanguageDocumentEditor();
    this.documentEditor.setObjectMapper(objectMapper);
  }

  private MultilanguageDocument createMultilanguageDocument(String filename) {
    try {
      String json = createJson(filename);
      MultilanguageDocument multilanguageDocument = objectMapper.readValue(json, MultilanguageDocument.class);
      return multilanguageDocument;
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
   * Test of getAsText method, of class MultilanguageDocumentEditor.
   */
  @Test
  public void testGetAsText() {
    System.out.println("getAsText");

    String expResult = createJson("multilanguageDocument.json").replaceAll("\\s", "");
    MultilanguageDocument multilanguageDocument = createMultilanguageDocument("multilanguageDocument.json");
    documentEditor.setValue(multilanguageDocument);

    String result = documentEditor.getAsText().replaceAll("\\s", "");
    assertEquals(expResult, result);
  }

  /**
   * Test of setAsText method, of class MultilanguageDocumentEditor.
   */
  @Test
  public void testSetAsText() {
    System.out.println("setAsText");

    MultilanguageDocument expResult = createMultilanguageDocument("multilanguageDocument.json");
    String json = createJson("multilanguageDocument.json");
    documentEditor.setAsText(json);

    MultilanguageDocumentImpl result = (MultilanguageDocumentImpl) documentEditor.getValue();
    assertEquals(expResult.getDocuments().size(), result.getDocuments().size());
  }

}
