package de.digitalcollections.cms.model.impl.entity;

import de.digitalcollections.cms.model.api.Text;
import de.digitalcollections.cms.model.api.entity.Entity;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class EntityImpl<ID extends Serializable> implements Entity<ID> {

  private ID id;
  private Text label;
  private Date lastModified;
  private String uuid = UUID.randomUUID().toString();

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
  public String getUuid() {
    return uuid;
  }

  @Override
  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  @Override
  public Date getLastModified() {
    return lastModified;
  }

  @Override
  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }
}
