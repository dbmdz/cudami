package de.digitalcollections.cms.model.api.entity;

import de.digitalcollections.cms.model.api.Text;
import java.io.Serializable;

/**
 * @param <ID> unique id specifying instance
 */
public interface Entity<ID extends Serializable> {

  ID getId();

  void setId(ID id);

  Text getLabel();

  void setLabel(Text label);

  String getUuid();

  void setUuid(String uuid);
}
