package de.digitalcollections.model.text.contentblock;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** A mark for marking special text (like html-tags "em", "strong") */
public class Mark {

  Map<String, Object> attributes = null;
  String type;

  public Mark() {}

  public Mark(String type) {
    this.type = type;
  }

  public void addAttribute(String key, Object value) {
    if (attributes == null) {
      attributes = new HashMap<>(0);
    }

    attributes.put(key, value);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Mark)) {
      return false;
    }
    Mark mark = (Mark) o;
    return Objects.equals(type, mark.type);
  }

  public Object getAttribute(String key) {
    if (attributes == null) {
      return null;
    }

    return attributes.get(key);
  }

  public Map<String, Object> getAttributes() {
    return attributes;
  }

  public String getType() {
    return type;
  }

  @Override
  public int hashCode() {

    return Objects.hash(type);
  }

  public void setAttributes(Map<String, Object> attributes) {
    this.attributes = attributes;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{type='" + type + "'}";
  }
}
