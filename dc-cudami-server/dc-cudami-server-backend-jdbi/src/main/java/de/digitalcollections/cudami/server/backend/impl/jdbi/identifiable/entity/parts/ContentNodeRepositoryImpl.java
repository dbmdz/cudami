package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.parts;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.parts.ContentNodeRepository;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.parts.ContentNode;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.EntityImpl;
import de.digitalcollections.model.impl.identifiable.entity.parts.ContentNodeImpl;
import de.digitalcollections.model.impl.identifiable.resource.FileResourceImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ContentNodeRepositoryImpl<E extends Entity>
    extends EntityPartRepositoryImpl<ContentNode, E> implements ContentNodeRepository<E> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ContentNodeRepositoryImpl.class);

  @Autowired
  public ContentNodeRepositoryImpl(Jdbi dbi) {
    super(dbi);
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM contentnodes";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOne().get());
    return count;
  }

  @Override
  public PageResponse<ContentNode> find(PageRequest pageRequest) {
    StringBuilder query =
        new StringBuilder(
            "SELECT uuid, label, description, created, last_modified FROM contentnodes");
    addPageRequestParams(pageRequest, query);

    List<ContentNodeImpl> result =
        dbi.withHandle(
            h -> h.createQuery(query.toString()).mapToBean(ContentNodeImpl.class).list());
    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public ContentNode findOne(UUID uuid) {
    String query =
        "SELECT uuid, label, description, created, last_modified FROM contentnodes WHERE uuid = :uuid";
    ContentNode contentNode =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bind("uuid", uuid)
                    .mapToBean(ContentNodeImpl.class)
                    .findOne()
                    .orElse(null));
    if (contentNode != null) {
      contentNode.setChildren(getChildren(contentNode));
    }
    return contentNode;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[] {"uuid"};
  }

  @Override
  public List<ContentNode> getChildren(ContentNode contentNode) {
    return getChildren(contentNode.getUuid());
  }

  @Override
  public List<ContentNode> getChildren(UUID uuid) {
    // minimal data required (= identifiable fields) for creating text links/teasers in a list
    String query =
        "SELECT "
            + "uuid, label, description, created, last_modified"
            + " FROM contentnodes INNER JOIN contentnode_contentnodes cc ON uuid = cc.child_contentnode_uuid"
            + " WHERE cc.parent_contentnode_uuid = :uuid"
            + " ORDER BY cc.sortIndex ASC";

    List<ContentNodeImpl> list =
        dbi.withHandle(
            h -> h.createQuery(query).bind("uuid", uuid).mapToBean(ContentNodeImpl.class).list());
    return list.stream().map(s -> (ContentNode) s).collect(Collectors.toList());
  }

  @Override
  public LinkedHashSet<E> getEntities(ContentNode contentNode) {
    return getEntities(contentNode.getUuid());
  }

  @Override
  public LinkedHashSet<E> getEntities(UUID contentNodeUuid) {
    // minimal data required (= identifiable fields) for creating text links/teasers in a list
    String query =
        "SELECT"
            + " uuid, label, description, entity_type, created, last_modified"
            + " FROM entities INNER JOIN contentnode_entities ce ON uuid = ce.entity_uuid"
            + " WHERE ce.contentnode_uuid = :uuid"
            + " ORDER BY ce.sortIndex ASC";

    List<EntityImpl> list =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bind("uuid", contentNodeUuid)
                    .mapToBean(EntityImpl.class)
                    .list());
    // TODO maybe does not work, then we have to refactor to LinkedHashSet<Entity>...
    LinkedHashSet<E> result =
        list.stream().map(s -> (E) s).collect(Collectors.toCollection(LinkedHashSet::new));
    return result;
  }

  @Override
  public LinkedHashSet<FileResource> getFileResources(ContentNode contentNode) {
    return getFileResources(contentNode.getUuid());
  }

  @Override
  public LinkedHashSet<FileResource> getFileResources(UUID contentNodeUuid) {
    // minimal data required (= identifiable fields) for creating text links/teasers in a list
    String query =
        "SELECT"
            + " uuid, label, description, created, last_modified,"
            + " filename, mimetype, size_in_bytes, uri"
            + " FROM fileresources INNER JOIN contentnode_fileresources cf ON uuid = cf.fileresource_uuid"
            + " WHERE cf.contentnode_uuid = :uuid"
            + " ORDER BY cf.sortIndex ASC";

    List<FileResourceImpl> list =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bind("uuid", contentNodeUuid)
                    .mapToBean(FileResourceImpl.class)
                    .list());
    LinkedHashSet<FileResource> result =
        list.stream()
            .map(s -> (FileResource) s)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    return result;
  }

  @Override
  public ContentNode getParent(UUID uuid) {
    String query =
        "SELECT"
            + " uuid, label, description, created, last_modified"
            + " FROM contentnodes"
            + " INNER JOIN contentnode_contentnodes cc ON uuid = cc.parent_contentnode_uuid"
            + " WHERE cc.child_contentnode_uuid = :uuid";

    ContentNode contentNode =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bind("uuid", uuid)
                    .mapToBean(ContentNodeImpl.class)
                    .findOne()
                    .orElse(null));
    return contentNode;
  }

  @Override
  public ContentNode save(ContentNode contentNode) {
    contentNode.setUuid(UUID.randomUUID());
    contentNode.setCreated(LocalDateTime.now());
    contentNode.setLastModified(LocalDateTime.now());

    String query =
        "INSERT INTO contentnodes("
            + "uuid, label, description, identifiable_type, created, last_modified"
            + ") VALUES ("
            + ":uuid, :label::JSONB, :description::JSONB, :type, :created, :lastModified"
            + ") RETURNING *";
    ContentNode result =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bindBean(contentNode)
                    .mapToBean(ContentNodeImpl.class)
                    .findOne()
                    .orElse(null));
    return result;
  }

  @Override
  public LinkedHashSet<E> saveEntities(ContentNode contentNode, LinkedHashSet<E> entities) {
    return saveEntities(contentNode.getUuid(), entities);
  }

  @Override
  public LinkedHashSet<E> saveEntities(UUID contentNodeUuid, LinkedHashSet<E> entities) {
    // as we store the whole list new: delete old entries
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM contentnode_entities WHERE contentnode_uuid = :uuid")
                .bind("uuid", contentNodeUuid)
                .execute());

    if (entities != null) {
      // we assume that the entities are already saved...
      dbi.useHandle(
          handle -> {
            PreparedBatch preparedBatch =
                handle.prepareBatch(
                    "INSERT INTO contentnode_entities(contentnode_uuid, entity_uuid, sortIndex) VALUES(:uuid, :entityUuid, :sortIndex)");
            for (Entity entity : entities) {
              preparedBatch
                  .bind("uuid", contentNodeUuid)
                  .bind("entityUuid", entity.getUuid())
                  .bind("sortIndex", getIndex(entities, entity))
                  .add();
            }
            preparedBatch.execute();
          });
    }
    return getEntities(contentNodeUuid);
  }

  @Override
  public LinkedHashSet<FileResource> saveFileResources(
      ContentNode contentNode, LinkedHashSet<FileResource> fileResources) {
    return saveFileResources(contentNode.getUuid(), fileResources);
  }

  @Override
  public LinkedHashSet<FileResource> saveFileResources(
      UUID contentNodeUuid, LinkedHashSet<FileResource> fileResources) {
    // as we store the whole list new: delete old entries
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM contentnode_fileresources WHERE contentnode_uuid = :uuid")
                .bind("uuid", contentNodeUuid)
                .execute());

    if (fileResources != null) {
      // we assume that the fileresources are already saved... so commented
      //      for (FileResource fileResource : fileResources) {
      //        cudamiFileResourceRepository.save(fileResource);
      //      }

      dbi.useHandle(
          handle -> {
            PreparedBatch preparedBatch =
                handle.prepareBatch(
                    "INSERT INTO contentnode_fileresources(contentnode_uuid, fileresource_uuid, sortIndex) VALUES(:uuid, :fileResourceUuid, :sortIndex)");
            for (FileResource fileResource : fileResources) {
              preparedBatch
                  .bind("uuid", contentNodeUuid)
                  .bind("fileResourceUuid", fileResource.getUuid())
                  .bind("sortIndex", getIndex(fileResources, fileResource))
                  .add();
            }
            preparedBatch.execute();
          });
    }
    return getFileResources(contentNodeUuid);
  }

  @Override
  public ContentNode saveWithParentContentNode(
      ContentNode contentNode, UUID parentContentNodeUuid) {
    ContentNode savedContentNode = save(contentNode);

    Integer sortindex =
        selectNextSortIndexForParentChildren(
            dbi, "contentnode_contentnodes", "parent_contentnode_uuid", parentContentNodeUuid);
    dbi.withHandle(
        h ->
            h.createUpdate(
                    "INSERT INTO contentnode_contentnodes(parent_contentnode_uuid, child_contentnode_uuid, sortindex)"
                        + " VALUES (:parent_contentnode_uuid, :uuid, :sortindex)")
                .bind("parent_contentnode_uuid", parentContentNodeUuid)
                .bind("sortindex", sortindex)
                .bindBean(savedContentNode)
                .execute());

    return findOne(savedContentNode.getUuid());
  }

  @Override
  public ContentNode saveWithParentContentTree(
      ContentNode contentNode, UUID parentContentTreeUuid) {
    ContentNode savedContentNode = save(contentNode);

    Integer sortindex =
        selectNextSortIndexForParentChildren(
            dbi, "contenttree_contentnodes", "contenttree_uuid", parentContentTreeUuid);
    dbi.withHandle(
        h ->
            h.createUpdate(
                    "INSERT INTO contenttree_contentnodes(contenttree_uuid, contentnode_uuid, sortindex)"
                        + " VALUES (:parent_contenttree_uuid, :uuid, :sortindex)")
                .bind("parent_contenttree_uuid", parentContentTreeUuid)
                .bind("sortindex", sortindex)
                .bindBean(savedContentNode)
                .execute());

    return findOne(savedContentNode.getUuid());
  }

  @Override
  public ContentNode update(ContentNode contentNode) {
    contentNode.setLastModified(LocalDateTime.now());

    // do not update/left out from statement (not changed since insert): uuid, created,
    // identifiable_type
    ContentNode result =
        dbi.withHandle(
            h ->
                h.createQuery(
                        "UPDATE contentnodes SET description=:description::JSONB, label=:label::JSONB, last_modified=:lastModified WHERE uuid=:uuid RETURNING *")
                    .bindBean(contentNode)
                    .mapToBean(ContentNodeImpl.class)
                    .findOne()
                    .orElse(null));
    return result;
  }
}
