package de.digitalcollections.cudami.admin.propertyeditor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.model.api.identifiable.parts.structuredcontent.LocalizedStructuredContent;
import java.beans.PropertyEditorSupport;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LocalizedStructuredContentEditor extends PropertyEditorSupport {

  private static final Logger LOGGER = LoggerFactory.getLogger(LocalizedStructuredContentEditor.class);

  @Autowired
  private ObjectMapper objectMapper;

  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public String getAsText() {
    LocalizedStructuredContent localizedStructuredContent = (LocalizedStructuredContent) getValue();
    try {
      return objectMapper.writeValueAsString(localizedStructuredContent);
    } catch (JsonProcessingException ex) {
      LOGGER.warn("Problem converting LocalizedStructuredContent to JSON-String", ex);
      return null;
    }
  }

  @Override
  public void setAsText(String json) {
    try {
      LocalizedStructuredContent localizedStructuredContent = objectMapper.readValue(json, LocalizedStructuredContent.class);
      setValue(localizedStructuredContent);
    } catch (IOException ex) {
      LOGGER.warn("Problem converting JSON-String to LocalizedStructuredContent", ex);
    }
  }
}
