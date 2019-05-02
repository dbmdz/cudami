package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.parts;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.parts.ContentNodeRepository;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.parts.ContentNode;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.parts.ContentNodeImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ContentNodeRepositoryImpl<E extends Entity> extends EntityPartRepositoryImpl<ContentNode, E> implements ContentNodeRepository<E> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ContentNodeRepositoryImpl.class);

  @Autowired
  public ContentNodeRepositoryImpl(Jdbi dbi) {
    super(dbi);
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM contentnodes";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOnly());
    return count;
  }

  @Override
  public PageResponse<ContentNode> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder()
        .append("SELECT " + IDENTIFIABLE_COLUMNS)
        .append(" FROM contentnodes");

    addPageRequestParams(pageRequest, query);

    List<ContentNodeImpl> result = dbi.withHandle(h -> h.createQuery(query.toString())
        .mapToBean(ContentNodeImpl.class)
        .list());
    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public ContentNode findOne(UUID uuid) {
    StringBuilder query = new StringBuilder()
        .append("SELECT " + IDENTIFIABLE_COLUMNS)
        .append(" FROM contentnodes")
        .append(" WHERE uuid = :uuid");

    ContentNode contentNode = dbi.withHandle(h -> h.createQuery(query.toString())
        .bind("uuid", uuid)
        .mapToBean(ContentNodeImpl.class)
        .findOnly());
    if (contentNode != null) {
      contentNode.setChildren(getChildren(contentNode));
    }
    return contentNode;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[]{"uuid"};
  }

  @Override
  public ContentNode save(ContentNode contentNode) {
    contentNode.setUuid(UUID.randomUUID());
    contentNode.setCreated(LocalDateTime.now());
    contentNode.setLastModified(LocalDateTime.now());

    ContentNode result = dbi.withHandle(h -> h
        .createQuery("INSERT INTO contentnodes(uuid, created, description, identifiable_type, label, last_modified) VALUES (:uuid, :created, :description::JSONB, :type, :label::JSONB, :lastModified) RETURNING *")
        .bindBean(contentNode)
        .mapToBean(ContentNodeImpl.class)
        .findOnly());
    return result;
  }

  @Override
  public ContentNode saveWithParentContentTree(ContentNode contentNode, UUID parentContentTreeUuid) {
    ContentNode savedContentNode = save(contentNode);

    Integer sortindex = selectNextSortIndexForParentChildren(dbi, "contenttree_contentnode", "contenttree_uuid", parentContentTreeUuid);
    dbi.withHandle(h -> h.createUpdate(
        "INSERT INTO contenttree_contentnode(contenttree_uuid, contentnode_uuid, sortindex)"
        + " VALUES (:parent_contenttree_uuid, :uuid, :sortindex)")
        .bind("parent_contenttree_uuid", parentContentTreeUuid)
        .bind("sortindex", sortindex)
        .bindBean(savedContentNode)
        .execute());

    return findOne(savedContentNode.getUuid());
  }

  @Override
  public ContentNode saveWithParentContentNode(ContentNode contentNode, UUID parentContentNodeUuid) {
    ContentNode savedContentNode = save(contentNode);

    Integer sortindex = selectNextSortIndexForParentChildren(dbi, "contentnode_contentnode", "parent_contentnode_uuid", parentContentNodeUuid);
    dbi.withHandle(h -> h.createUpdate(
        "INSERT INTO contentnode_contentnode(parent_contentnode_uuid, child_contentnode_uuid, sortindex)"
        + " VALUES (:parent_contentnode_uuid, :uuid, :sortindex)")
        .bind("parent_contentnode_uuid", parentContentNodeUuid)
        .bind("sortindex", sortindex)
        .bindBean(savedContentNode)
        .execute());

    return findOne(savedContentNode.getUuid());
  }

  @Override
  public ContentNode update(ContentNode contentNode) {
    contentNode.setLastModified(LocalDateTime.now());

    // do not update/left out from statement (not changed since insert): uuid, created, identifiable_type, entity_type
    ContentNode result = dbi.withHandle(h -> h
        .createQuery("UPDATE contentnodes SET description=:description::JSONB, label=:label::JSONB, last_modified=:lastModified WHERE uuid=:uuid RETURNING *")
        .bindBean(contentNode)
        .mapToBean(ContentNodeImpl.class)
        .findOnly());
    return result;
  }

  @Override
  public List<ContentNode> getChildren(ContentNode contentNode) {
    return getChildren(contentNode.getUuid());
  }

  @Override
  public List<ContentNode> getChildren(UUID uuid) {
    // minimal data required (= identifiable fields) for creating text links/teasers in a list
    String sql = "SELECT " + IDENTIFIABLE_COLUMNS
                 + " FROM contentnodes INNER JOIN contentnode_contentnodes cc ON uuid = cc.child_contentnode_uuid"
                 + " WHERE cc.parent_contentnode_uuid = :uuid"
                 + " ORDER BY cc.sortIndex ASC";

//    StringBuilder query = new StringBuilder()
//        .append("SELECT i.uuid as uuid, i.label as label")
//        .append(" FROM contentnodes cn INNER JOIN contentnode_contentnode cc ON cn.uuid=cc.parent_contentnode_uuid INNER JOIN identifiables i ON cc.child_contentnode_uuid=i.uuid")
//        .append(" WHERE cn.uuid = :uuid")
//        .append(" ORDER BY cc.sortindex ASC");
    List<ContentNodeImpl> list = dbi.withHandle(h -> h.createQuery(sql)
        .bind("uuid", uuid)
        .mapToBean(ContentNodeImpl.class)
        .list());

    if (list.isEmpty()) {
      return new ArrayList<>();
    }
    return list.stream().map(s -> (ContentNode) s).collect(Collectors.toList());
  }

  @Override
  public LinkedHashSet<E> getEntities(ContentNode contentNode) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public LinkedHashSet<E> getEntities(UUID contentNodeUuid) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public LinkedHashSet<FileResource> getFileResources(ContentNode contentNode) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public LinkedHashSet<FileResource> getFileResources(UUID contentNodeUuid) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public LinkedHashSet<E> saveEntities(ContentNode contentNode, LinkedHashSet<E> entities) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public LinkedHashSet<E> saveEntities(UUID contentNodeUuid, LinkedHashSet<E> entities) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public LinkedHashSet<FileResource> saveFileResources(ContentNode contentNode, LinkedHashSet<FileResource> fileResources) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public LinkedHashSet<FileResource> saveFileResources(UUID contentNodeUuid, LinkedHashSet<FileResource> fileResources) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

//  @Override
//  public List<Identifiable> getIdentifiables(C contentNode) {
//    return getIdentifiables(contentNode.getUuid());
//  }
//
//  @Override
//  public List<Identifiable> getIdentifiables(UUID uuid) {
//    // minimal data required for creating text links in a list
//    String query = "SELECT i.uuid as uuid, i.label as label"
//                   + " FROM identifiables i INNER JOIN contentnode_identifiables ci ON ci.identifiable_uuid=i.uuid"
//                   + " WHERE ci.contentnode_uuid = :uuid"
//                   + " ORDER BY ci.sortindex ASC";
//
//    List<IdentifiableImpl> list = dbi.withHandle(h -> h.createQuery(query)
//        .bind("uuid", uuid)
//        .mapToBean(IdentifiableImpl.class)
//        .list());
//
//    if (list.isEmpty()) {
//      return new ArrayList<>();
//    }
//    return list.stream().map(Identifiable.class::cast).collect(Collectors.toList());
//  }
//
//  @Override
//  public void addIdentifiable(UUID identifiablesContainerUuid, UUID identifiableUuid) {
//    Integer sortindex = selectNextSortIndexForParentChildren(dbi, "contentnode_identifiables", "contentnode_uuid", identifiablesContainerUuid);
//    dbi.withHandle(h -> h.createUpdate(
//        "INSERT INTO contentnode_identifiables(contentnode_uuid, identifiable_uuid, sortindex)"
//        + " VALUES (:contentnode_uuid, :identifiable_uuid, :sortindex)")
//        .bind("contentnode_uuid", identifiablesContainerUuid)
//        .bind("identifiable_uuid", identifiableUuid)
//        .bind("sortindex", sortindex)
//        .execute());
//  }
//
//  @Override
//  public List<Identifiable> saveIdentifiables(C contentNode, List<Identifiable> identifiables) {
//    UUID uuid = contentNode.getUuid();
//    return saveIdentifiables(uuid, identifiables);
//  }
//
//  @Override
//  public List<Identifiable> saveIdentifiables(UUID identifiablesContainerUuid, List<Identifiable> identifiables) {
//    dbi.withHandle(h -> h.createUpdate("DELETE FROM contentnode_identifiables WHERE contentnode_uuid = :uuid")
//        .bind("uuid", identifiablesContainerUuid).execute());
//
//    dbi.useHandle(handle -> {
//      PreparedBatch preparedBatch = handle.prepareBatch("INSERT INTO contentnode_identifiables(contentnode_uuid, identifiable_uuid, sortindex) VALUES(:uuid, :identifiableUuid, :sortindex)");
//      for (Identifiable identifiable : identifiables) {
//        preparedBatch.bind("uuid", identifiablesContainerUuid)
//            .bind("identifiableUuid", identifiable.getUuid())
//            .bind("sortindex", identifiables.indexOf(identifiable))
//            .add();
//      }
//      preparedBatch.execute();
//    });
//    return getIdentifiables(identifiablesContainerUuid);
//  }
}
