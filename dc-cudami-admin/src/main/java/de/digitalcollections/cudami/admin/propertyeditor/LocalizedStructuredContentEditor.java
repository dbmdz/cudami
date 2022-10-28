package de.digitalcollections.cudami.admin.propertyeditor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.beans.PropertyEditorSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@SuppressFBWarnings
public class LocalizedStructuredContentEditor extends PropertyEditorSupport {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(LocalizedStructuredContentEditor.class);

  private final ObjectMapper objectMapper;

  @Autowired
  public LocalizedStructuredContentEditor(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public String getAsText() {
    LocalizedStructuredContent localizedStructuredContent = (LocalizedStructuredContent) getValue();
    String text = "";
    if (localizedStructuredContent != null) {
      try {
        text = objectMapper.writeValueAsString(localizedStructuredContent);
      } catch (JsonProcessingException ex) {
        LOGGER.warn("Problem converting LocalizedStructuredContent to JSON-String", ex);
      }
    }
    return text;
  }

  @Override
  public void setAsText(String json) {
    LocalizedStructuredContent localizedStructuredContent = null;
    if (json != null && !json.isEmpty()) {
      try {
        localizedStructuredContent = objectMapper.readValue(json, LocalizedStructuredContent.class);
      } catch (JsonProcessingException ex) {
        LOGGER.warn("Problem converting JSON-String to LocalizedStructuredContent", ex);
      }
    }
    setValue(localizedStructuredContent);
  }
}
