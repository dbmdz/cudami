package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.ContentTreeRepository;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.ContentTree;
import de.digitalcollections.model.api.identifiable.entity.parts.ContentNode;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.ContentTreeImpl;
import de.digitalcollections.model.impl.identifiable.entity.parts.ContentNodeImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ContentTreeRepositoryImpl extends EntityRepositoryImpl<ContentTree>
    implements ContentTreeRepository {

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
    StringBuilder query =
        new StringBuilder(
            "SELECT uuid, label, description, created, last_modified FROM contenttrees");
    addPageRequestParams(pageRequest, query);

    List<ContentTreeImpl> result =
        dbi.withHandle(
            h -> h.createQuery(query.toString()).mapToBean(ContentTreeImpl.class).list());
    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public ContentTree findOne(UUID uuid) {
    String query =
        "SELECT uuid, label, description, created, last_modified FROM contenttrees WHERE uuid = :uuid";
    ContentTree contentTree =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bind("uuid", uuid)
                    .mapToBean(ContentTreeImpl.class)
                    .findOne()
                    .orElse(null));
    if (contentTree != null) {
      contentTree.setRootNodes(getRootNodes(contentTree));
    }
    return contentTree;
  }

  @Override
  public ContentTree findOne(Identifier identifier) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return null;
  }

  @Override
  public List<ContentNode> getRootNodes(ContentTree contentTree) {
    UUID uuid = contentTree.getUuid();
    return getRootNodes(uuid);
  }

  @Override
  public List<ContentNode> getRootNodes(UUID uuid) {
    // minimal data required (= identifiable fields) for creating text links/teasers in a list
    String query =
        "SELECT "
            + "uuid, label, description, created, last_modified"
            + " FROM contentnodes INNER JOIN contenttree_contentnodes cc ON uuid = cc.contentnode_uuid"
            + " WHERE cc.contenttree_uuid = :uuid"
            + " ORDER BY cc.sortIndex ASC";
    List<ContentNodeImpl> list =
        dbi.withHandle(
            h -> h.createQuery(query).bind("uuid", uuid).mapToBean(ContentNodeImpl.class).list());
    return list.stream().map(ContentNode.class::cast).collect(Collectors.toList());
  }

  @Override
  public ContentTree save(ContentTree contentTree) {
    contentTree.setUuid(UUID.randomUUID());
    contentTree.setCreated(LocalDateTime.now());
    contentTree.setLastModified(LocalDateTime.now());

    String query =
        "INSERT INTO contenttrees("
            + "uuid, label, description, identifiable_type, entity_type, created, last_modified"
            + ") VALUES ("
            + ":uuid, :label::JSONB, :description::JSONB, :type, :entityType, :created, :lastModified"
            + ") RETURNING *";
    ContentTree result =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bindBean(contentTree)
                    .mapToBean(ContentTreeImpl.class)
                    .findOne()
                    .orElse(null));
    return result;
  }

  @Override
  public ContentTree update(ContentTree contentTree) {
    contentTree.setLastModified(LocalDateTime.now());

    // do not update/left out from statement (not changed since insert): uuid, created,
    // identifiable_type, entity_type
    String query =
        "UPDATE contenttrees SET"
            + " label=:label::JSONB, description=:description::JSONB, last_modified=:lastModified"
            + " WHERE uuid=:uuid"
            + " RETURNING *";
    ContentTree result =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bindBean(contentTree)
                    .mapToBean(ContentTreeImpl.class)
                    .findOne()
                    .orElse(null));
    return result;
  }
}
