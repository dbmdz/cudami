package de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.IdentifiableRepository;
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

  /**
   * Save list of entities related to an entity.Prerequisites: entities have been saved before (exist already) and subject is for all relations the same
   * @param relations a list of entity-predicate-entity relations
   * @return the list of the relations for the same subject-entity
   */
  List<EntityRelation> saveRelations(List<EntityRelation> relations);

  void addRelatedFileresource(E entity, FileResource fileResource);

  void addRelatedFileresource(UUID entityUuid, UUID fileResourceUuid);

  LinkedHashSet<FileResource> getRelatedFileResources(E entity);

  LinkedHashSet<FileResource> getRelatedFileResources(UUID entityUuid);

  /**
   * Save list of file resources related to an entity. Prerequisite: file resources have been saved before (exist already)
   * @param entity entity the fileresources are related to
   * @param fileResources the fileresources that are related to the entity
   * @return the list of the related fileresources
   */
  LinkedHashSet<FileResource> saveRelatedFileResources(E entity, LinkedHashSet<FileResource> fileResources);

  LinkedHashSet<FileResource> saveRelatedFileResources(UUID entityUuid, LinkedHashSet<FileResource> fileResources);
}
