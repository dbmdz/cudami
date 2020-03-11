package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.ContentTreeRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.IdentifiableAggregator;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.ContentTree;
import de.digitalcollections.model.api.identifiable.entity.parts.ContentNode;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.IdentifierImpl;
import de.digitalcollections.model.impl.identifiable.entity.ContentTreeImpl;
import de.digitalcollections.model.impl.identifiable.entity.parts.ContentNodeImpl;
import de.digitalcollections.model.impl.identifiable.resource.ImageFileResourceImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ContentTreeRepositoryImpl extends EntityRepositoryImpl<ContentTree>
    implements ContentTreeRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(ContentTreeRepositoryImpl.class);

  // select all details shown/needed in single object details page
  private static final String FIND_ONE_BASE_SQL =
      "SELECT c.uuid c_uuid, c.refid c_refId, c.label c_label, c.description c_description,"
          + " c.identifiable_type c_type, c.entity_type c_entityType,"
          + " c.created c_created, c.last_modified c_lastModified,"
          + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
          + " file.uuid f_uuid, file.filename f_filename, file.mimetype f_mimetype, file.size_in_bytes f_size_in_bytes, file.uri f_uri"
          + " FROM contenttrees as c"
          + " LEFT JOIN identifiers as id on c.uuid = id.identifiable"
          + " LEFT JOIN fileresources_image as file on c.previewfileresource = file.uuid";

  // select only what is shown/needed in paged list (to avoid unnecessary payload/traffic):
  private static final String REDUCED_FIND_ONE_BASE_SQL =
      "SELECT c.uuid c_uuid, c.refid c_refId, c.label c_label, c.description c_description,"
          + " c.identifiable_type c_type, c.entity_type c_entityType,"
          + " c.created c_created, c.last_modified c_lastModified,"
          + " file.uuid f_uuid, file.filename f_filename, file.uri f_uri"
          + " FROM contenttrees as c"
          + " LEFT JOIN fileresources_image as file on c.previewfileresource = file.uuid";

  @Autowired
  public ContentTreeRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository) {
    super(dbi, identifierRepository);
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM contenttrees";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOne().get());
    return count;
  }

  @Override
  public PageResponse<ContentTree> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder(REDUCED_FIND_ONE_BASE_SQL);
    addPageRequestParams(pageRequest, query);

    List<ContentTreeImpl> result =
        dbi.withHandle(
            h ->
                h.createQuery(query.toString())
                    .registerRowMapper(BeanMapper.factory(ContentTreeImpl.class, "c"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                    .reduceRows(
                        new LinkedHashMap<UUID, IdentifiableAggregator<ContentTreeImpl>>(),
                        (map, rowView) -> {
                          IdentifiableAggregator<ContentTreeImpl> aggregator =
                              map.computeIfAbsent(
                                  rowView.getColumn("c_uuid", UUID.class),
                                  fn -> {
                                    return new IdentifiableAggregator<>(
                                        rowView.getRow(ContentTreeImpl.class));
                                  });
                          ContentTreeImpl obj = aggregator.identifiable;
                          if (rowView.getColumn("f_uuid", UUID.class) != null) {
                            obj.setPreviewImage(rowView.getRow(ImageFileResourceImpl.class));
                          }
                          return map;
                        })
                    .values().stream()
                    .map(aggregator -> aggregator.identifiable)
                    .collect(Collectors.toList()));

    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public ContentTree findOne(UUID uuid) {
    String query = FIND_ONE_BASE_SQL + " WHERE c.uuid = :uuid";

    ContentTreeImpl result =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bind("uuid", uuid)
                    .registerRowMapper(BeanMapper.factory(ContentTreeImpl.class, "c"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                    .reduceRows(
                        new IdentifiableAggregator<ContentTreeImpl>(),
                        (aggregator, rowView) -> {
                          if (aggregator.identifiable == null) {
                            aggregator.identifiable = rowView.getRow(ContentTreeImpl.class);
                          }
                          ContentTreeImpl obj = aggregator.identifiable;

                          if (rowView.getColumn("f_uuid", UUID.class) != null) {
                            obj.setPreviewImage(rowView.getRow(ImageFileResourceImpl.class));
                          }

                          final UUID idUuid = rowView.getColumn("id_uuid", UUID.class);
                          if (idUuid != null && !aggregator.identifiers.contains(idUuid)) {
                            IdentifierImpl identifier = rowView.getRow(IdentifierImpl.class);
                            obj.addIdentifier(identifier);
                            aggregator.identifiers.add(idUuid);
                          }

                          return aggregator;
                        })
                    .identifiable);
    if (result != null) {
      // TODO could be replaced with another join in above query...
      result.setRootNodes(getRootNodes(result));
    }
    return result;
  }

  @Override
  public ContentTree findOne(Identifier identifier) {
    if (identifier.getIdentifiable() != null) {
      return findOne(identifier.getIdentifiable());
    }

    String namespace = identifier.getNamespace();
    String identifierId = identifier.getId();

    String query = FIND_ONE_BASE_SQL + " WHERE id.identifier = :id AND id.namespace = :namespace";

    ContentTreeImpl result =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bind("id", identifierId)
                    .bind("namespace", namespace)
                    .registerRowMapper(BeanMapper.factory(ContentTreeImpl.class, "c"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                    .reduceRows(
                        new IdentifiableAggregator<ContentTreeImpl>(),
                        (aggregator, rowView) -> {
                          if (aggregator.identifiable == null) {
                            aggregator.identifiable = rowView.getRow(ContentTreeImpl.class);
                          }
                          ContentTreeImpl obj = aggregator.identifiable;

                          if (rowView.getColumn("f_uuid", UUID.class) != null) {
                            obj.setPreviewImage(rowView.getRow(ImageFileResourceImpl.class));
                          }

                          final UUID idUuid = rowView.getColumn("id_uuid", UUID.class);
                          if (idUuid != null && !aggregator.identifiers.contains(idUuid)) {
                            IdentifierImpl newIdentifier = rowView.getRow(IdentifierImpl.class);
                            obj.addIdentifier(newIdentifier);
                            aggregator.identifiers.add(idUuid);
                          }

                          return aggregator;
                        })
                    .identifiable);
    if (result != null) {
      // TODO could be replaced with another join in above query...
      result.setRootNodes(getRootNodes(result));
    }
    return result;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[] {"c.created", "c.last_modified", "c.refid"};
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
        "SELECT cn.uuid cn_uuid, cn.label cn_label, cn.description cn_description,"
            + " cn.identifiable_type cn_type,"
            + " cn.created cn_created, cn.last_modified cn_lastModified,"
            + " file.uuid f_uuid, file.uri f_uri, file.filename f_filename"
            + " FROM contentnodes as cn INNER JOIN contenttree_contentnodes cc ON cn.uuid = cc.contentnode_uuid"
            + " LEFT JOIN fileresources_image as file on cn.previewfileresource = file.uuid"
            + " WHERE cc.contenttree_uuid = :uuid"
            + " ORDER BY cc.sortIndex ASC";

    List<ContentNode> result =
        dbi.withHandle(
            h ->
                h.createQuery(query).bind("uuid", uuid)
                    .registerRowMapper(BeanMapper.factory(ContentNodeImpl.class, "cn"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                    .reduceRows(
                        new LinkedHashMap<UUID, IdentifiableAggregator<ContentNodeImpl>>(),
                        (map, rowView) -> {
                          IdentifiableAggregator<ContentNodeImpl> aggregator =
                              map.computeIfAbsent(
                                  rowView.getColumn("cn_uuid", UUID.class),
                                  fn -> {
                                    return new IdentifiableAggregator<>(
                                        rowView.getRow(ContentNodeImpl.class));
                                  });
                          ContentNodeImpl obj = aggregator.identifiable;
                          if (rowView.getColumn("f_uuid", UUID.class) != null) {
                            obj.setPreviewImage(rowView.getRow(ImageFileResourceImpl.class));
                          }
                          return map;
                        })
                    .values().stream()
                    .map(aggregator -> (ContentNode) aggregator.identifiable)
                    .collect(Collectors.toList()));
    return result;
  }

  @Override
  public ContentTree save(ContentTree contentTree) {
    contentTree.setUuid(UUID.randomUUID());
    contentTree.setCreated(LocalDateTime.now());
    contentTree.setLastModified(LocalDateTime.now());
    // refid is generated as serial, DO NOT SET!
    final UUID previewImageUuid =
        contentTree.getPreviewImage() == null ? null : contentTree.getPreviewImage().getUuid();

    String query =
        "INSERT INTO contenttrees("
            + "uuid, label, description, previewfileresource,"
            + " identifiable_type, entity_type,"
            + " created, last_modified"
            + ") VALUES ("
            + ":uuid, :label::JSONB, :description::JSONB, :previewFileResource,"
            + " :type, :entityType,"
            + " :created, :lastModified"
            + ")";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(contentTree)
                .execute());

    // save identifiers
    List<Identifier> identifiers = contentTree.getIdentifiers();
    saveIdentifiers(identifiers, contentTree);

    ContentTree result = findOne(contentTree.getUuid());
    return result;
  }

  @Override
  public ContentTree update(ContentTree contentTree) {
    contentTree.setLastModified(LocalDateTime.now());

    // do not update/left out from statement (not changed since insert):
    // uuid, created, identifiable_type, entity_type, refid
    final UUID previewImageUuid =
        contentTree.getPreviewImage() == null ? null : contentTree.getPreviewImage().getUuid();

    String query =
        "UPDATE contenttrees SET"
            + " label=:label::JSONB, description=:description::JSONB, previewfileresource=:previewFileResource,"
            + " last_modified=:lastModified"
            + " WHERE uuid=:uuid";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(contentTree)
                .execute());

    // save identifiers
    // as we store the whole list new: delete old entries
    deleteIdentifiers(contentTree);
    List<Identifier> identifiers = contentTree.getIdentifiers();
    saveIdentifiers(identifiers, contentTree);

    ContentTree result = findOne(contentTree.getUuid());
    return result;
  }
}
