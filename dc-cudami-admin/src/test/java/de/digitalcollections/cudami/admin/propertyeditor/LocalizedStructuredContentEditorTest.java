package de.digitalcollections.cudami.admin.propertyeditor;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.admin.test.TestApplication;
import de.digitalcollections.cudami.admin.test.config.SpringConfigBusinessForTest;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    classes = {TestApplication.class, SpringConfigBusinessForTest.class},
    webEnvironment = WebEnvironment.RANDOM_PORT)
public class LocalizedStructuredContentEditorTest implements InitializingBean {

  @Autowired private ObjectMapper objectMapper;

  private LocalizedStructuredContentEditor documentEditor;

  @Override
  public void afterPropertiesSet() throws Exception {
    this.documentEditor = new LocalizedStructuredContentEditor(objectMapper);
  }

  private LocalizedStructuredContent createLocalizedStructuredContent(String filename) {
    try {
      String json = createJson(filename);
      LocalizedStructuredContent lsc =
          objectMapper.readValue(json, LocalizedStructuredContent.class);
      return lsc;
    } catch (IOException ex) {
      return null;
    }
  }

  private String createJson(String filename) {
    try {
      String json =
          IOUtils.toString(
              this.getClass().getClassLoader().getResourceAsStream(filename),
              StandardCharsets.UTF_8);
      return json;
    } catch (IOException ex) {
      return null;
    }
  }

  /** Test of getAsText method, of class LocalizedStructuredContentEditor. */
  @Test
  public void testGetAsText() {
    String expResult = createJson("localizedStructuredContent.json").replaceAll("\\s", "");
    LocalizedStructuredContent lsc =
        createLocalizedStructuredContent("localizedStructuredContent.json");
    documentEditor.setValue(lsc);

    String result = documentEditor.getAsText().replaceAll("\\s", "");
    assertThat(result).isEqualTo(expResult);
  }

  /** Test of setAsText method, of class LocalizedStructuredContentEditor. */
  @Test
  public void testSetAsText() {
    LocalizedStructuredContent expResult =
        createLocalizedStructuredContent("localizedStructuredContent.json");
    String json = createJson("localizedStructuredContent.json");
    documentEditor.setAsText(json);

    LocalizedStructuredContent result = (LocalizedStructuredContent) documentEditor.getValue();
    assertThat(result.size()).isEqualTo(expResult.size());
  }
}
