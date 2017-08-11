package de.digitalcollections.cudami.model.impl.entity;

import de.digitalcollections.cudami.model.api.Text;
import de.digitalcollections.cudami.model.api.Thumbnail;
import de.digitalcollections.cudami.model.api.entity.Entity;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class EntityImpl<ID extends Serializable> implements Entity<ID> {

  private Text description;
  private ID id;
  private Text label;
  private Date lastModified = new Date();
  private Thumbnail thumbnail;
  private UUID uuid = UUID.randomUUID();

  @Override
  public Text getDescription() {
    return description;
  }

  @Override
  public void setDescription(Text description) {
    this.description = description;
  }

  @Override
  public ID getId() {
    return id;
  }

  @Override
  public void setId(ID id) {
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
  public Date getLastModified() {
    return lastModified;
  }

  @Override
  public void setLastModified(Date lastModified) {
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
