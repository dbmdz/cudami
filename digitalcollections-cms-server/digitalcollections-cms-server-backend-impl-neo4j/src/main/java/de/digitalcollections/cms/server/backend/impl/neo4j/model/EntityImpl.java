package de.digitalcollections.cms.server.backend.impl.neo4j.model;

import de.digitalcollections.cms.model.api.Entity;
import java.util.UUID;
import org.neo4j.ogm.annotation.GraphId;

public abstract class EntityImpl implements Entity {

  @GraphId
  private Long graphId;

  private String uuid = UUID.randomUUID().toString();

  public Long getGraphId() {
    return graphId;
  }

  public void setGraphId(Long graphId) {
    this.graphId = graphId;
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
    return "Entity{" + "id=" + graphId + '}';
  }

}
