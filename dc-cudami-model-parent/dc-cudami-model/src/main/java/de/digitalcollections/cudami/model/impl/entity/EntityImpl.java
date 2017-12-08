package de.digitalcollections.cudami.model.impl.entity;

import de.digitalcollections.cudami.model.api.Text;
import de.digitalcollections.cudami.model.api.Thumbnail;
import de.digitalcollections.cudami.model.api.entity.Entity;
import de.digitalcollections.cudami.model.api.enums.EntityType;
import de.digitalcollections.cudami.model.impl.identifiable.IdentifiableImpl;
import java.time.LocalDateTime;

public class EntityImpl extends IdentifiableImpl implements Entity {

  private LocalDateTime created;
  private Text description;
  protected EntityType entityType;
  private Text label;
  private LocalDateTime lastModified;
  private Thumbnail thumbnail;

  @Override
  public LocalDateTime getCreated() {
    return created;
  }

  @Override
  public void setCreated(LocalDateTime created) {
    this.created = created;
  }

  @Override
  public Text getDescription() {
    return description;
  }

  @Override
  public void setDescription(Text description) {
    this.description = description;
  }

  @Override
  public EntityType getEntityType() {
    return entityType;
  }

  @Override
  public void setEntityType(EntityType entityType) {
    this.entityType = entityType;
  }

  @Override
  public Text getLabel() {
    return label;
  }

  @Override
  public void setLabel(Text label) {
    this.label = label;
  }

  @Override
  public LocalDateTime getLastModified() {
    return lastModified;
  }

  @Override
  public void setLastModified(LocalDateTime lastModified) {
    this.lastModified = lastModified;
  }

  @Override
  public Thumbnail getThumbnail() {
    return thumbnail;
  }

  @Override
  public void setThumbnail(Thumbnail thumbnail) {
    this.thumbnail = thumbnail;
  }
}
