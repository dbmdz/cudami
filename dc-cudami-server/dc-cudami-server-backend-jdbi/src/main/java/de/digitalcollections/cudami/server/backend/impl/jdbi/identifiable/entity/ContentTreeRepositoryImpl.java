package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.ContentTreeRepository;
import de.digitalcollections.model.api.identifiable.entity.ContentTree;
import de.digitalcollections.model.api.identifiable.entity.parts.ContentNode;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.ContentTreeImpl;
import de.digitalcollections.model.impl.identifiable.entity.parts.ContentNodeImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ContentTreeRepositoryImpl extends EntityRepositoryImpl<ContentTree> implements ContentTreeRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(ContentTreeRepositoryImpl.class);

  @Autowired
  public ContentTreeRepositoryImpl(Jdbi dbi) {
    super(dbi);
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM contenttrees";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOne().get());
    return count;
  }

  @Override
  public PageResponse<ContentTree> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder("SELECT " + IDENTIFIABLE_COLUMNS)
      .append(" FROM contenttrees");

    addPageRequestParams(pageRequest, query);

    List<ContentTreeImpl> result = dbi.withHandle(h -> h.createQuery(query.toString())
      .mapToBean(ContentTreeImpl.class)
      .list());
    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public ContentTree findOne(UUID uuid) {
    String query = "SELECT " + IDENTIFIABLE_COLUMNS
                   + " FROM contenttrees"
                   + " WHERE uuid = :uuid";

    ContentTree contentTree = dbi.withHandle(h -> h.createQuery(query)
      .bind("uuid", uuid)
      .mapToBean(ContentTreeImpl.class)
      .findOne().orElse(null));
    if (contentTree != null) {
      contentTree.setRootNodes(getRootNodes(contentTree));
    }
    return contentTree;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return null;
  }

  @Override
  public ContentTree save(ContentTree contentTree) {
    contentTree.setUuid(UUID.randomUUID());
    contentTree.setCreated(LocalDateTime.now());
    contentTree.setLastModified(LocalDateTime.now());

    ContentTree result = dbi.withHandle(h -> h
      .createQuery("INSERT INTO contenttrees(uuid, created, description, identifiable_type, label, last_modified, entity_type) VALUES (:uuid, :created, :description::JSONB, :type, :label::JSONB, :lastModified, :entityType) RETURNING *")
      .bindBean(contentTree)
      .mapToBean(ContentTreeImpl.class)
      .findOne().orElse(null));
    return result;
  }

  @Override
  public ContentTree update(ContentTree contentTree) {
    contentTree.setLastModified(LocalDateTime.now());

    // do not update/left out from statement (not changed since insert): uuid, created, identifiable_type, entity_type
    ContentTree result = dbi.withHandle(h -> h
      .createQuery("UPDATE contenttrees SET description=:description::JSONB, label=:label::JSONB, last_modified=:lastModified WHERE uuid=:uuid RETURNING *")
      .bindBean(contentTree)
      .mapToBean(ContentTreeImpl.class)
      .findOne().orElse(null));
    return result;
  }

  @Override
  public List<ContentNode> getRootNodes(ContentTree contentTree) {
    UUID uuid = contentTree.getUuid();
    return getRootNodes(uuid);
  }

  @Override
  public List<ContentNode> getRootNodes(UUID uuid) {
    // minimal data required (= identifiable fields) for creating text links/teasers in a list
    String sql = "SELECT " + IDENTIFIABLE_COLUMNS
                 + " FROM contentnodes INNER JOIN contenttree_contentnodes cc ON uuid = cc.contentnode_uuid"
                 + " WHERE cc.contenttree_uuid = :uuid"
                 + " ORDER BY cc.sortIndex ASC";

//    String query = "SELECT cc.contentnode_uuid as uuid, i.label as label"
//                   + " FROM contenttrees ct INNER JOIN contenttree_contentnode cc ON ct.uuid=cc.contenttree_uuid INNER JOIN identifiables i ON cc.contentnode_uuid=i.uuid"
//                   + " WHERE ct.uuid = :uuid"
//                   + " ORDER BY cc.sortIndex ASC";
    List<ContentNodeImpl> list = dbi.withHandle(h -> h.createQuery(sql)
      .bind("uuid", uuid)
      .mapToBean(ContentNodeImpl.class)
      .list());

    if (list.isEmpty()) {
      return new ArrayList<>();
    }
    return list.stream().map(ContentNode.class::cast).collect(Collectors.toList());
  }
}
