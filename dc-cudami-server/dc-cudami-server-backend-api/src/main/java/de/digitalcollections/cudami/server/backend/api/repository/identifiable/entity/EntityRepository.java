package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.EntityRelation;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

/**
 * @param <E> entity instance
 */
public interface EntityRepository<E extends Entity> extends IdentifiableRepository<E> {

  void addRelation(EntityRelation<E> relation);

  void addRelation(UUID subjectEntityUuid, String predicate, UUID objectEntityUuid);

  List<EntityRelation> getRelations(E subjectEntity);

  List<EntityRelation> getRelations(UUID subjectEntityUuid);

  List<EntityRelation> saveRelations(List<EntityRelation> relations);

  void addRelatedFileresource(E entity, FileResource fileResource);

  void addRelatedFileresource(UUID entityUuid, UUID fileResourceUuid);

  LinkedHashSet<FileResource> getRelatedFileResources(E entity);

  LinkedHashSet<FileResource> getRelatedFileResources(UUID entityUuid);

  LinkedHashSet<FileResource> saveRelatedFileResources(E entity, LinkedHashSet<FileResource> fileResources);

  LinkedHashSet<FileResource> saveRelatedFileResources(UUID entityUuid, LinkedHashSet<UUID> fileResourcesUuids);
}
