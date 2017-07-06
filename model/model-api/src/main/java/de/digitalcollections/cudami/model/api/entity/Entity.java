package de.digitalcollections.cudami.model.api.entity;

import de.digitalcollections.cudami.model.api.Text;
import java.io.Serializable;
import java.util.Date;

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
  
  Date getLastModified();
  
  void setLastModified(Date lastModified);
}
