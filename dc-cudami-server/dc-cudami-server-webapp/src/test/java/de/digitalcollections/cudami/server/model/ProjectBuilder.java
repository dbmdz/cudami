package de.digitalcollections.cudami.server.model;

import de.digitalcollections.model.identifiable.entity.EntityType;
import de.digitalcollections.model.identifiable.entity.Project;

public class ProjectBuilder extends EntityBuilder<Project, ProjectBuilder> {

  @Override
  protected Project createEntity() {
    return new Project();
  }

  @Override
  protected EntityType getEntityType() {
    return EntityType.PROJECT;
  }
}
