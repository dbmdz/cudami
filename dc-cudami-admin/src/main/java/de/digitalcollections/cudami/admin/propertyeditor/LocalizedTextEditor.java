package de.digitalcollections.cudami.admin.propertyeditor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.model.text.LocalizedText;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.beans.PropertyEditorSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@SuppressFBWarnings
public class LocalizedTextEditor extends PropertyEditorSupport {

  private static final Logger LOGGER = LoggerFactory.getLogger(LocalizedTextEditor.class);

  private final ObjectMapper objectMapper;

  @Autowired
  public LocalizedTextEditor(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public String getAsText() {
    LocalizedText localizedText = (LocalizedText) getValue();
    String text = "";
    if (localizedText != null) {
      try {
        text = objectMapper.writeValueAsString(localizedText);
      } catch (JsonProcessingException ex) {
        LOGGER.warn("Problem converting LocalizedText to JSON-String", ex);
      }
    }
    return text;
  }

  @Override
  public void setAsText(String json) {
    try {
      LocalizedText localizedText = objectMapper.readValue(json, LocalizedText.class);
      setValue(localizedText);
    } catch (JsonProcessingException ex) {
      LOGGER.warn("Problem converting JSON-String to LocalizedText", ex);
    }
  }
}
