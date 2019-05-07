package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.parts;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.parts.EntityPartRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.parts.EntityPart;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import java.util.LinkedHashSet;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class EntityPartRepositoryImpl<P extends EntityPart, E extends Entity> extends IdentifiableRepositoryImpl<P> implements EntityPartRepository<P, E> {

  @Autowired
  public EntityPartRepositoryImpl(Jdbi dbi) {
    super(dbi);
  }

  @Override
  public void addRelatedEntity(EntityPart entityPart, Entity entity) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void addRelatedEntity(UUID entityPartUuid, UUID entityUuid) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void addRelatedFileresource(EntityPart entityPart, FileResource fileResource) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void addRelatedFileresource(UUID entityPartUuid, UUID fileResourceUuid) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public LinkedHashSet getRelatedEntities(EntityPart entityPart) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public LinkedHashSet getRelatedEntities(UUID entityPartUuid) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public LinkedHashSet getRelatedFileResources(EntityPart entityPart) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public LinkedHashSet getRelatedFileResources(UUID entityPartUuid) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public LinkedHashSet saveRelatedEntities(EntityPart entityPart, LinkedHashSet entities) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public LinkedHashSet saveRelatedEntities(UUID entityPartUuid, LinkedHashSet entities) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public LinkedHashSet saveRelatedFileResources(EntityPart entityPart, LinkedHashSet fileResources) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public LinkedHashSet saveRelatedFileResources(UUID entityPartUuid, LinkedHashSet fileResources) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

}
