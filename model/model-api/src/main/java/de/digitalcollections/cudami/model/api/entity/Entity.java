package de.digitalcollections.cudami.model.api.entity;

import de.digitalcollections.cudami.model.api.Identifiable;
import de.digitalcollections.cudami.model.api.Text;
import java.io.Serializable;
import java.util.Date;

/**
 * An entity.
 *
 * @param <ID> unique serializable identifier
 */
public interface Entity<ID extends Serializable> extends Identifiable<ID> {

  Text getLabel();

  void setLabel(Text label);

  String getUuid();

  void setUuid(String uuid);

  Date getLastModified();

  void setLastModified(Date lastModified);
}
