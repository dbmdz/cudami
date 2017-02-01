package de.digitalcollections.cms.server.backend.impl.neo4j.model.entity;

import com.google.gson.Gson;
import de.digitalcollections.cms.model.api.Text;
import de.digitalcollections.cms.model.api.entity.Entity;
import java.util.UUID;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.Relationship;

public class EntityImpl implements Entity {

  @GraphId
  private Long graphId;

  @Relationship(type = "HAS_LABEL")
  private Text label;

  private String uuid = UUID.randomUUID().toString();

  public Long getGraphId() {
    return graphId;
  }

  public void setGraphId(Long graphId) {
    this.graphId = graphId;
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
  public String toString() {
    Gson gson = new Gson();
    String json = gson.toJson(this);
    return json;
  }

}
