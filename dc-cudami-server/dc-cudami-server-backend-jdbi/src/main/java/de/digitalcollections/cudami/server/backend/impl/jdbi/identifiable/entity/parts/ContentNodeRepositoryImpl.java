package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.parts;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.parts.ContentNodeRepository;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.parts.ContentNode;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.IdentifierImpl;
import de.digitalcollections.model.impl.identifiable.entity.EntityImpl;
import de.digitalcollections.model.impl.identifiable.entity.parts.ContentNodeImpl;
import de.digitalcollections.model.impl.identifiable.resource.FileResourceImpl;
import de.digitalcollections.model.impl.identifiable.resource.ImageFileResourceImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ContentNodeRepositoryImpl<E extends Entity>
    extends EntityPartRepositoryImpl<ContentNode, E> implements ContentNodeRepository<E> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ContentNodeRepositoryImpl.class);

  // select all details shown/needed in single object details page
  private static final String FIND_ONE_BASE_SQL =
      "SELECT c.uuid c_uuid, c.label c_label, c.description c_description,"
          + " c.identifiable_type c_type,"
          + " c.created c_created, c.last_modified c_lastModified,"
          + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
          + " file.filename f_filename, file.mimetype f_mimetype, file.size_in_bytes f_size_in_bytes, file.uri f_uri"
          + " FROM contentnodes as c"
          + " LEFT JOIN identifiers as id on c.uuid = id.identifiable"
          + " LEFT JOIN fileresources_image as file on c.previewfileresource = file.uuid";

  // select only what is shown/needed in paged list (to avoid unnecessary payload/traffic):
  private static final String REDUCED_FIND_ONE_BASE_SQL =
      "SELECT c.uuid c_uuid, c.refid c_refId, c.label c_label, c.description c_description,"
          + " c.identifiable_type c_type,"
          + " c.created c_created, c.last_modified c_lastModified,"
          + " file.uri f_uri, file.filename f_filename"
          + " FROM contentnodes as c"
          + " LEFT JOIN fileresources_image as file on c.previewfileresource = file.uuid";

  @Autowired
  public ContentNodeRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository) {
    super(dbi, identifierRepository);
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM contentnodes";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOne().get());
    return count;
  }

  @Override
  public PageResponse<ContentNode> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder(REDUCED_FIND_ONE_BASE_SQL);
    addPageRequestParams(pageRequest, query);

    List<ContentNodeImpl> result =
        dbi.withHandle(
            h ->
                h.createQuery(query.toString())
                    .registerRowMapper(BeanMapper.factory(ContentNodeImpl.class, "c"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                    .reduceRows(
                        new LinkedHashMap<UUID, ContentNodeImpl>(),
                        (map, rowView) ->
                            addPreviewImage(map, rowView, ContentNodeImpl.class, "c_uuid"))
                    .values().stream()
                    .collect(Collectors.toList()));
    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public ContentNode findOne(UUID uuid) {
    String query = FIND_ONE_BASE_SQL + " WHERE c.uuid = :uuid";

    Optional<ContentNodeImpl> resultOpt =
        dbi.withHandle(
            h ->
                h.createQuery(query).bind("uuid", uuid)
                    .registerRowMapper(BeanMapper.factory(ContentNodeImpl.class, "c"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                    .reduceRows(
                        new LinkedHashMap<UUID, ContentNodeImpl>(),
                        (map, rowView) ->
                            addPreviewImageAndIdentifiers(
                                map, rowView, ContentNodeImpl.class, "c_uuid"))
                    .values().stream()
                    .findFirst());
    if (!resultOpt.isPresent()) {
      return null;
    }
    ContentNode contentNode = resultOpt.get();
    if (contentNode != null) {
      // TODO could be replaced with another join in above query...
      contentNode.setChildren(getChildren(contentNode));
    }
    return contentNode;
  }

  @Override
  public ContentNode findOne(Identifier identifier) {
    if (identifier.getIdentifiable() != null) {
      return findOne(identifier.getIdentifiable());
    }

    String namespace = identifier.getNamespace();
    String identifierId = identifier.getId();

    String query = FIND_ONE_BASE_SQL + " WHERE id.identifier = :id AND id.namespace = :namespace";

    Optional<ContentNodeImpl> resultOpt =
        dbi.withHandle(
            h ->
                h.createQuery(query).bind("id", identifierId).bind("namespace", namespace)
                    .registerRowMapper(BeanMapper.factory(ContentNodeImpl.class, "c"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                    .reduceRows(
                        new LinkedHashMap<UUID, ContentNodeImpl>(),
                        (map, rowView) ->
                            addPreviewImageAndIdentifiers(
                                map, rowView, ContentNodeImpl.class, "c_uuid"))
                    .values().stream()
                    .findFirst());
    if (!resultOpt.isPresent()) {
      return null;
    }
    ContentNode contentNode = resultOpt.get();
    if (contentNode != null) {
      // TODO could be replaced with another join in above query...
      contentNode.setChildren(getChildren(contentNode));
    }
    return contentNode;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[] {"c.created", "c.last_modified"};
  }

  @Override
  public List<ContentNode> getChildren(ContentNode contentNode) {
    return getChildren(contentNode.getUuid());
  }

  @Override
  public List<ContentNode> getChildren(UUID uuid) {
    // minimal data required (= identifiable fields) for creating text links/teasers in a list
    String query =
        "SELECT cn.uuid cn_uuid, cn.label cn_label, cn.description cn_description,"
            + " cn.identifiable_type cn_type,"
            + " cn.created cn_created, cn.last_modified cn_lastModified,"
            + " file.uri f_uri, file.filename f_filename"
            + " FROM contentnodes as cn INNER JOIN contentnode_contentnodes cc ON cn.uuid = cc.child_contentnode_uuid"
            + " LEFT JOIN fileresources_image as file on cn.previewfileresource = file.uuid"
            + " WHERE cc.parent_contentnode_uuid = :uuid"
            + " ORDER BY cc.sortIndex ASC";

    List<ContentNodeImpl> result =
        dbi.withHandle(
            h ->
                h.createQuery(query).bind("uuid", uuid)
                    .registerRowMapper(BeanMapper.factory(ContentNodeImpl.class, "cn"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                    .reduceRows(
                        new LinkedHashMap<UUID, ContentNodeImpl>(),
                        (map, rowView) ->
                            addPreviewImage(map, rowView, ContentNodeImpl.class, "cn_uuid"))
                    .values().stream()
                    .collect(Collectors.toList()));
    return result.stream().map(ContentNode.class::cast).collect(Collectors.toList());
  }

  @Override
  public LinkedHashSet<E> getEntities(ContentNode contentNode) {
    return getEntities(contentNode.getUuid());
  }

  @Override
  public LinkedHashSet<E> getEntities(UUID contentNodeUuid) {
    // minimal data required (= identifiable fields) for creating text links/teasers in a list
    String query =
        "SELECT e.uuid e_uuid, e.refid e_refId, e.label e_label, e.description e_description,"
            + " e.identifiable_type e_type, e.entity_type e_entityType,"
            + " e.created e_created, e.last_modified e_lastModified,"
            + " file.uri f_uri, file.filename f_filename"
            + " FROM entities as e INNER JOIN contentnode_entities ce ON e.uuid = ce.entity_uuid"
            + " LEFT JOIN fileresources_image as file on e.previewfileresource = file.uuid"
            + " WHERE ce.contentnode_uuid = :uuid"
            + " ORDER BY ce.sortIndex ASC";

    List<EntityImpl> result =
        dbi.withHandle(
            h ->
                h.createQuery(query).bind("uuid", contentNodeUuid)
                    .registerRowMapper(BeanMapper.factory(EntityImpl.class, "e"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                    .reduceRows(
                        new LinkedHashMap<UUID, EntityImpl>(),
                        (map, rowView) -> addPreviewImage(map, rowView, EntityImpl.class, "e_uuid"))
                    .values().stream()
                    .collect(Collectors.toList()));
    return result.stream().map(s -> (E) s).collect(Collectors.toCollection(LinkedHashSet::new));
  }

  @Override
  public LinkedHashSet<FileResource> getFileResources(ContentNode contentNode) {
    return getFileResources(contentNode.getUuid());
  }

  @Override
  public LinkedHashSet<FileResource> getFileResources(UUID contentNodeUuid) {
    // minimal data required (= identifiable fields) for creating text links/teasers in a list
    String query =
        "SELECT f.uuid f_uuid, f.label f_label, f.description f_description,"
            + " f.identifiable_type f_type,"
            + " f.created f_created, f.last_modified f_lastModified,"
            + " f.filename f_filename, f.mimetype f_mimeType,"
            + " pf.uri pf_uri, pf.filename pf_filename"
            + " FROM fileresources as f INNER JOIN contentnode_fileresources cf ON f.uuid = cf.fileresource_uuid"
            + " LEFT JOIN fileresources_image as pf on f.previewfileresource = pf.uuid"
            + " WHERE cf.contentnode_uuid = :uuid"
            + " ORDER BY cf.sortIndex ASC";

    List<FileResourceImpl> list =
        dbi.withHandle(
            h ->
                h.createQuery(query).bind("uuid", contentNodeUuid)
                    .registerRowMapper(BeanMapper.factory(FileResourceImpl.class, "cn"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                    .reduceRows(
                        new LinkedHashMap<UUID, FileResourceImpl>(),
                        (map, rowView) ->
                            addPreviewImage(map, rowView, FileResourceImpl.class, "cn_uuid"))
                    .values().stream()
                    .collect(Collectors.toList()));
    // TODO it is an implementation: better return List? (LinkedHashSet was used because of getting
    // guaranteed sorting)
    LinkedHashSet<FileResource> result =
        list.stream()
            .map(s -> (FileResource) s)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    return result;
  }

  @Override
  public ContentNode getParent(UUID uuid) {
    String query =
        REDUCED_FIND_ONE_BASE_SQL
            + " INNER JOIN contentnode_contentnodes cc ON c.uuid = cc.parent_contentnode_uuid"
            + " WHERE cc.child_contentnode_uuid = :uuid";

    Optional<ContentNodeImpl> resultOpt =
        dbi.withHandle(
            h ->
                h.createQuery(query).bind("uuid", uuid)
                    .registerRowMapper(BeanMapper.factory(ContentNodeImpl.class, "c"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                    .reduceRows(
                        new LinkedHashMap<UUID, ContentNodeImpl>(),
                        (map, rowView) ->
                            addPreviewImage(map, rowView, ContentNodeImpl.class, "c_uuid"))
                    .values().stream()
                    .findFirst());
    if (!resultOpt.isPresent()) {
      return null;
    }
    ContentNode contentNode = resultOpt.get();
    return contentNode;
  }

  @Override
  public ContentNode save(ContentNode contentNode) {
    contentNode.setUuid(UUID.randomUUID());
    contentNode.setCreated(LocalDateTime.now());
    contentNode.setLastModified(LocalDateTime.now());
    final UUID previewImageUuid =
        contentNode.getPreviewImage() == null ? null : contentNode.getPreviewImage().getUuid();

    String query =
        "INSERT INTO contentnodes("
            + "uuid, label, description, previewfileresource,"
            + " identifiable_type,"
            + " created, last_modified"
            + ") VALUES ("
            + ":uuid, :label::JSONB, :description::JSONB, :previewFileResource,"
            + " :type,"
            + " :created, :lastModified"
            + ")";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(contentNode)
                .execute());

    // save identifiers
    List<Identifier> identifiers = contentNode.getIdentifiers();
    saveIdentifiers(identifiers, contentNode);

    ContentNode result = findOne(contentNode.getUuid());
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
      // we assume that the fileresources are already saved...
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

    // do not update/left out from statement (not changed since insert):
    // uuid, created, identifiable_type
    final UUID previewImageUuid =
        contentNode.getPreviewImage() == null ? null : contentNode.getPreviewImage().getUuid();

    String query =
        "UPDATE contentnodes SET"
            + " label=:label::JSONB, description=:description::JSONB, previewfileresource=:previewFileResource,"
            + " last_modified=:lastModified"
            + " WHERE uuid=:uuid";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(contentNode)
                .execute());

    // save identifiers
    // as we store the whole list new: delete old entries
    deleteIdentifiers(contentNode);
    List<Identifier> identifiers = contentNode.getIdentifiers();
    saveIdentifiers(identifiers, contentNode);

    ContentNode result = findOne(contentNode.getUuid());
    return result;
  }
}
