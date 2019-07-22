package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.EntityRelation;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.EntityImpl;
import de.digitalcollections.model.impl.identifiable.resource.FileResourceImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class EntityRepositoryImpl<E extends Entity> extends IdentifiableRepositoryImpl<E> implements EntityRepository<E> {

  @Autowired
  public EntityRepositoryImpl(Jdbi dbi) {
    super(dbi);
  }

  @Override
  public void addRelatedFileresource(E entity, FileResource fileResource) {
    addRelatedFileresource(entity.getUuid(), fileResource.getUuid());
  }

  @Override
  public void addRelatedFileresource(UUID entityUuid, UUID fileResourceUuid) {
    Integer sortIndex = selectNextSortIndexForParentChildren(dbi, "rel_entity_fileresources", "entity_uuid", entityUuid);
    dbi.withHandle(h -> h.createUpdate("INSERT INTO rel_entity_fileresources(entity_uuid, fileresource_uuid, sortindex) VALUES (:entity_uuid, :fileresource_uuid, :sortindex)")
        .bind("entity_uuid", entityUuid)
        .bind("fileresource_uuid", fileResourceUuid)
        .bind("sortindex", sortIndex)
        .execute());
  }

  @Override
  public void addRelation(EntityRelation<E> relation) {
    addRelation(relation.getSubject().getUuid(), relation.getPredicate(), relation.getObject().getUuid());
  }

  @Override
  public void addRelation(UUID subjectEntityUuid, String predicate, UUID objectEntityUuid) {
    dbi.withHandle(h -> h.createUpdate("INSERT INTO rel_entity_entities(subject_uuid, predicate, object_uuid) VALUES (:subject_uuid, :predicate, :object_uuid)")
        .bind("subject_uuid", subjectEntityUuid)
        .bind("predicate", predicate)
        .bind("object_uuid", objectEntityUuid)
        .execute());
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM entities";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOne().get());
    return count;
  }

  @Override
  public PageResponse<E> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder("SELECT " + IDENTIFIABLE_COLUMNS + ", entityType")
        .append(" FROM entities");

    addPageRequestParams(pageRequest, query);
    List<EntityImpl> result = dbi.withHandle(h -> h.createQuery(query.toString())
        .mapToBean(EntityImpl.class)
        .list());
    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public E findOne(UUID uuid) {
    String query = "SELECT " + IDENTIFIABLE_COLUMNS + ", entityType"
                   + " FROM entities"
                   + " WHERE uuid = :uuid";

    E entity = (E) dbi.withHandle(h -> h.createQuery(query)
        .bind("uuid", uuid)
        .mapToBean(EntityImpl.class)
        .findOne().orElse(null));
    return entity;
  }

  @Override
  public E findOne(Identifier identifier) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[]{"created", "entity_type", "last_modified"};
  }

  private int getIndex(LinkedHashSet<FileResource> fileResources, FileResource fileResource) {
    boolean found = false;
    int pos = -1;
    for (Iterator<FileResource> iterator = fileResources.iterator(); iterator.hasNext();) {
      pos = pos + 1;
      FileResource fr = iterator.next();
      if (fr.getUuid().equals(fileResource.getUuid())) {
        found = true;
        break;
      }
    }
    if (found) {
      return pos;
    }
    return -1;
  }

  @Override
  public LinkedHashSet<FileResource> getRelatedFileResources(E entity) {
    return getRelatedFileResources(entity.getUuid());
  }

  @Override
  public LinkedHashSet<FileResource> getRelatedFileResources(UUID entityUuid) {
    StringBuilder query = new StringBuilder("SELECT *")
        .append(" FROM fileresources f INNER JOIN rel_entity_fileresources ref ON f.uuid=ref.fileresource_uuid")
        .append(" WHERE ref.entity_uuid = :entityUuid")
        .append(" ORDER BY ref.sortindex");

    List<FileResourceImpl> result = dbi.withHandle(h -> h.createQuery(query.toString())
        .bind("entityUuid", entityUuid)
        .mapToBean(FileResourceImpl.class)
        .list());
    return new LinkedHashSet<>(result);
  }

  @Override
  public List<EntityRelation> getRelations(E subjectEntity) {
    // query predicate and object entity (subject entity is given)
    StringBuilder query = new StringBuilder("SELECT rel.predicate as predicate,")
        .append(" e.uuid as uuid, e.created as created, e.description as description, e.identifiable_type as identifiable_type, e.label as label, e.last_modified as last_modified, e.entity_type as entity_type")
        .append(" FROM rel_entity_entities rel INNER JOIN entities e ON rel.object_uuid=e.uuid")
        .append(" WHERE rel.subject_uuid = :uuid");

    List<EntityRelation> result = dbi.withHandle(h -> h.createQuery(query.toString())
        .bind("uuid", subjectEntity.getUuid())
        .map(new EntityRelationMapper(subjectEntity))
        .list());
    return result;
  }

  @Override
  public List<EntityRelation> getRelations(UUID subjectEntityUuid) {
    E subjectEntity = findOne(subjectEntityUuid);
    return getRelations(subjectEntity);
  }

  @Override
  public E save(E entity) {
    throw new UnsupportedOperationException("use save of specific/inherited entity repository");
  }

  @Override
  public LinkedHashSet<FileResource> saveRelatedFileResources(E entity, LinkedHashSet<FileResource> fileResources) {
    return saveRelatedFileResources(entity.getUuid(), fileResources);
  }

  @Override
  public LinkedHashSet<FileResource> saveRelatedFileResources(UUID entityUuid, LinkedHashSet<FileResource> fileResources) {
    if (fileResources == null) {
      return null;
    }
    // as we store the whole list new: delete old entries
    dbi.withHandle(h -> h.createUpdate("DELETE FROM rel_entity_fileresources WHERE entity_uuid = :uuid")
        .bind("uuid", entityUuid).execute());

    dbi.useHandle(handle -> {
      PreparedBatch preparedBatch = handle.prepareBatch("INSERT INTO rel_entity_fileresources(entity_uuid, fileresource_uuid, sortIndex) VALUES(:uuid, :fileResourceUuid, :sortIndex)");
      for (FileResource fileResource : fileResources) {
        preparedBatch.bind("uuid", entityUuid)
            .bind("fileResourceUuid", fileResource.getUuid())
            .bind("sortIndex", getIndex(fileResources, fileResource))
            .add();
      }
      preparedBatch.execute();
    });
    return getRelatedFileResources(entityUuid);
  }

  @Override
  public List<EntityRelation> saveRelations(List<EntityRelation> relations) {
    if (relations == null) {
      return null;
    }
    // get subject uuid:
    UUID subjectUuid = relations.get(0).getSubject().getUuid();
    // as we store the whole list new: delete old entries
    dbi.withHandle(h -> h.createUpdate("DELETE FROM rel_entity_entities WHERE entity_uuid = :uuid")
        .bind("uuid", subjectUuid)
        .execute());

    dbi.useHandle(handle -> {
      PreparedBatch preparedBatch = handle.prepareBatch("INSERT INTO rel_entity_entities(subject_uuid, predicate, object_uuid) VALUES(:subjectUuid, :predicate, :objectUuid)");
      for (EntityRelation relation : relations) {
        preparedBatch.bind("subjectUuid", subjectUuid)
            .bind("predicate", relation.getPredicate())
            .bind("objectUuid", relation.getObject().getUuid())
            .add();
      }
      preparedBatch.execute();
    });
    return getRelations(subjectUuid);
  }

  @Override
  public E update(E entity) {
    throw new UnsupportedOperationException("use update of specific/inherited entity repository");
  }
}
