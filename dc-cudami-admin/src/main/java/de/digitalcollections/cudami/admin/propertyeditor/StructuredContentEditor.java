package de.digitalcollections.cudami.admin.propertyeditor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.model.text.StructuredContent;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.beans.PropertyEditorSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@SuppressFBWarnings
public class StructuredContentEditor extends PropertyEditorSupport {

  private static final Logger LOGGER = LoggerFactory.getLogger(StructuredContentEditor.class);

  private final ObjectMapper objectMapper;

  @Autowired
  public StructuredContentEditor(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public String getAsText() {
    StructuredContent structuredContent = (StructuredContent) getValue();
    String text = "";
    if (structuredContent != null) {
      try {
        text = objectMapper.writeValueAsString(structuredContent);
      } catch (JsonProcessingException ex) {
        LOGGER.warn("Problem converting StructuredContent to JSON-String", ex);
      }
    }
    return text;
  }

  @Override
  public void setAsText(String json) {
    StructuredContent structuredContent = null;
    if (json != null && !json.isEmpty()) {
      try {
        structuredContent = objectMapper.readValue(json, StructuredContent.class);
      } catch (JsonProcessingException ex) {
        LOGGER.warn("Problem converting JSON-String to StructuredContent", ex);
      }
    }
    setValue(structuredContent);
  }
}
