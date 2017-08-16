package de.digitalcollections.cudami.model.api.entity;

import de.digitalcollections.cudami.model.api.Identifiable;
import de.digitalcollections.cudami.model.api.Text;
import de.digitalcollections.cudami.model.api.Thumbnail;
import de.digitalcollections.cudami.model.api.enums.EntityType;
import java.io.Serializable;
import java.util.Date;

/**
 * An entity.
 *
 * @param <ID> unique serializable identifier
 */
public interface Entity<ID extends Serializable> extends Identifiable<ID> {

  EntityType getEntityType();

  Text getDescription();

  void setDescription(Text description);

  Text getLabel();

  void setLabel(Text label);

  Date getLastModified();

  void setLastModified(Date lastModified);

  Thumbnail getThumbnail();

  void setThumbnail(Thumbnail thumbnail);
}
