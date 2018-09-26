package de.digitalcollections.cudami.admin.propertyeditor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.model.api.identifiable.parts.LocalizedText;
import java.beans.PropertyEditorSupport;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LocalizedTextEditor extends PropertyEditorSupport {

  private static final Logger LOGGER = LoggerFactory.getLogger(LocalizedTextEditor.class);

  @Autowired
  private ObjectMapper objectMapper;

  public void setObjectMapper(ObjectMapper objectMapper) {
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
    } catch (IOException ex) {
      LOGGER.warn("Problem converting JSON-String to LocalizedText", ex);
    }
  }
}
