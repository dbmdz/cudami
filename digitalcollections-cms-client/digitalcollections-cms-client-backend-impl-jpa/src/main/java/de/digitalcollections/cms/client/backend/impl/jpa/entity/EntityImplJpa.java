package de.digitalcollections.cms.client.backend.impl.jpa.entity;

import com.google.gson.Gson;
import de.digitalcollections.cms.model.api.Text;
import de.digitalcollections.cms.model.api.entity.Entity;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Transient;
import org.hibernate.validator.constraints.NotEmpty;

public abstract class EntityImplJpa implements Entity<Long> {

  @Transient
  private Text label;

  @NotEmpty
  @Column(name = "uuid", nullable = false, unique = true)
  private String uuid = UUID.randomUUID().toString();

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
  public String toString() {
    Gson gson = new Gson();
    String json = gson.toJson(this);
    return json;
  }

}
