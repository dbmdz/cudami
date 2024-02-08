package de.digitalcollections.cudami.admin.propertyeditor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.beans.PropertyEditorSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressFBWarnings
public class JsonObjectEditor extends PropertyEditorSupport {

  private static final Logger LOGGER = LoggerFactory.getLogger(JsonObjectEditor.class);

  private final Class<?> objectClass;
  private final ObjectMapper objectMapper;

  public JsonObjectEditor(ObjectMapper objectMapper, Class<?> objectClass) {
    this.objectClass = objectClass;
    this.objectMapper = objectMapper;
  }

  @Override
  public String getAsText() {
    Object value = getValue();
    String text = "";
    if (value != null) {
      try {
        text = objectMapper.writeValueAsString(value);
      } catch (JsonProcessingException ex) {
        LOGGER.warn(
            "Problem converting " + objectClass.getSimpleName() + " object to JSON-String", ex);
      }
    }
    return text;
  }

  @Override
  public void setAsText(String json) {
    Object object = null;
    if (json != null && !json.isEmpty()) {
      try {
        object = objectMapper.readValue(json, objectClass);
      } catch (JsonProcessingException ex) {
        LOGGER.warn(
            "Problem converting JSON-String to " + objectClass.getSimpleName() + " object", ex);
      }
    }
    setValue(object);
  }
}
