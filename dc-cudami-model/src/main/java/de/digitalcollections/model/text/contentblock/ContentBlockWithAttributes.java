package de.digitalcollections.model.text.contentblock;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class ContentBlockWithAttributes extends ContentBlock {

  Map<String, Object> attributes = null;

  public void addAttribute(String key, Object value) {
    if (attributes == null) {
      attributes = new HashMap<>(0);
    }
    if (value != null && key != null) {
      attributes.put(key, value);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ContentBlockWithAttributes that = (ContentBlockWithAttributes) o;
    return Objects.equals(attributes, that.attributes);
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

  @Override
  public int hashCode() {

    return Objects.hash(super.hashCode(), attributes);
  }

  public void setAttributes(Map<String, Object> attributes) {
    this.attributes = attributes;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" + "attributes=" + attributes + "}";
  }
}
