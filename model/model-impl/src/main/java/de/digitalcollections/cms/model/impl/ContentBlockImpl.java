package de.digitalcollections.cms.model.impl;

import de.digitalcollections.cms.model.api.ContentBlock;
import de.digitalcollections.cms.model.api.enums.ContentBlockType;
import java.util.HashMap;
import java.util.Map;

public class ContentBlockImpl implements ContentBlock {

  private ContentBlockType type;
  private final Map<String, Object> data = new HashMap<>();
  
  @Override
  public Object getData(String key) {
     return data.get(key);
  }

  @Override
  public Map<String, Object> getData() {
    return data;
  }

  @Override
  public Integer getIntegerData(String key) {
    return (Integer) data.get(key);
  }

  @Override
  public String getStringData(String key) {
    return (String) data.get(key);
  }

  @Override
  public ContentBlockType getType() {
    return type;
  }

  @Override
  public void setType(ContentBlockType type) {
    this.type = type;
  }

  @Override
  public void setData(String key, Object value) {
    data.put(key, value);
  }
  
}
