package de.digitalcollections.cudami.model.api;

import de.digitalcollections.cudami.model.api.enums.ContentBlockType;
import java.io.Serializable;
import java.util.Map;

/**
 * A content block of a specific type and type specific data.
 */
public interface ContentBlock extends Serializable {

  Object getData(String key);

  Map<String, Object> getData();

  Integer getIntegerData(String key);

  String getStringData(String key);

  ContentBlockType getType();

  void setType(ContentBlockType type);

  void setData(String key, Object value);

}
