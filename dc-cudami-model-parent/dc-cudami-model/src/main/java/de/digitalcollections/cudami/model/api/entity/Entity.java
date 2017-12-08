package de.digitalcollections.cudami.model.api.entity;

import de.digitalcollections.cudami.model.api.Text;
import de.digitalcollections.cudami.model.api.Thumbnail;
import de.digitalcollections.cudami.model.api.enums.EntityType;
import de.digitalcollections.cudami.model.api.identifiable.Identifiable;
import java.time.LocalDateTime;

/**
 * An entity.
 */
public interface Entity extends Identifiable {

  LocalDateTime getCreated();

  void setCreated(LocalDateTime created);

  EntityType getEntityType();

  void setEntityType(EntityType entityType);

  Text getDescription();

  void setDescription(Text description);

  Text getLabel();

  void setLabel(Text label);

  LocalDateTime getLastModified();

  void setLastModified(LocalDateTime lastModified);

  Thumbnail getThumbnail();

  void setThumbnail(Thumbnail thumbnail);
}
