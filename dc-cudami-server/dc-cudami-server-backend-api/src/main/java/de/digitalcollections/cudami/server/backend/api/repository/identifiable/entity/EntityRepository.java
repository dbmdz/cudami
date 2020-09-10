package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.EntityRelation;
import de.digitalcollections.model.api.identifiable.entity.enums.EntityType;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import java.util.List;
import java.util.UUID;

/** @param <E> entity instance */
public interface EntityRepository<E extends Entity> extends IdentifiableRepository<E> {

  void addRelation(EntityRelation relation);

  void addRelation(UUID subjectEntityUuid, String predicate, UUID objectEntityUuid);

  /**
   * Returns a list of all Entities, reduced to their identifiers and last modification date
   *
   * @param entityType the entity type, from which the source table is calculated
   * @return partially filled list of all entities
   */
  List<E> findAllReduced(EntityType entityType);

  E findOneByRefId(long refId);

  List<EntityRelation> getRelations(E subjectEntity);

  List<EntityRelation> getRelations(UUID subjectEntityUuid);

  /**
   * Save list of entities related to an entity.Prerequisites: entities have been saved before
   * (exist already) and subject is for all relations the same
   *
   * @param relations a list of entity-predicate-entity relations
   * @return the list of the relations for the same subject-entity
   */
  List<EntityRelation> saveRelations(List<EntityRelation> relations);

  void addRelatedFileresource(E entity, FileResource fileResource);

  void addRelatedFileresource(UUID entityUuid, UUID fileResourceUuid);

  List<FileResource> getRelatedFileResources(E entity);

  List<FileResource> getRelatedFileResources(UUID entityUuid);

  /**
   * Get GND data from an official GND-source and import as new entity.
   * 
   * @param gndId GND-ID of entity, e.g. "2007744-0" (DFG)
   * @return saved entity
   */
  E importByGndId(String gndId);

  /**
   * Save list of file resources related to an entity. Prerequisite: file resources have been saved
   * before (exist already)
   *
   * @param entity entity the fileresources are related to
   * @param fileResources the fileresources that are related to the entity
   * @return the list of the related fileresources
   */
  List<FileResource> saveRelatedFileResources(E entity, List<FileResource> fileResources);

  List<FileResource> saveRelatedFileResources(UUID entityUuid, List<FileResource> fileResources);
}
