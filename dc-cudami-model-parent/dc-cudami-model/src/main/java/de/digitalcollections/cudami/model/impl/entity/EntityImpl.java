package de.digitalcollections.cudami.model.impl.entity;

import de.digitalcollections.cudami.model.api.Text;
import de.digitalcollections.cudami.model.api.Thumbnail;
import de.digitalcollections.cudami.model.api.entity.Entity;
import de.digitalcollections.cudami.model.api.enums.EntityType;
import java.time.LocalDateTime;
import java.util.UUID;

public class EntityImpl implements Entity<Long> {

  private LocalDateTime created;
  private Text description;
  protected EntityType entityType;
  private Long id;
  private Text label;
  private LocalDateTime lastModified;
  private Thumbnail thumbnail;
  private UUID uuid = UUID.randomUUID();

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
  public Long getId() {
    return id;
  }

  @Override
  public void setId(Long id) {
    this.id = id;
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

  @Override
  public UUID getUuid() {
    return uuid;
  }

  @Override
  public void setUuid(UUID uuid) {
    this.uuid = uuid;
  }
}
