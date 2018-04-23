package de.digitalcollections.cudami.admin.propertyeditor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.model.api.identifiable.resource.MultilanguageDocument;
import java.beans.PropertyEditorSupport;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MultilanguageDocumentEditor extends PropertyEditorSupport {

  private static final Logger LOGGER = LoggerFactory.getLogger(MultilanguageDocumentEditor.class);

  @Autowired
  private ObjectMapper objectMapper;

  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public String getAsText() {
    MultilanguageDocument mld = (MultilanguageDocument) getValue();
    try {
      return objectMapper.writeValueAsString(mld);
    } catch (JsonProcessingException ex) {
      LOGGER.warn("Problem converting MultilanguageDocument to JSON-String", ex);
      return null;
    }
  }

  @Override
  public void setAsText(String json) {
    try {
      MultilanguageDocument mld = objectMapper.readValue(json, MultilanguageDocument.class);
      setValue(mld);
    } catch (IOException ex) {
      LOGGER.warn("Problem converting JSON-String to MultilanguageDocument", ex);
    }
  }
}
