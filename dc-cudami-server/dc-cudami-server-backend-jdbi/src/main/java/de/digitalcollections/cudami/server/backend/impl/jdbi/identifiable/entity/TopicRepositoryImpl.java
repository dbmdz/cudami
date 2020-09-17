package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.TopicRepository;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.Topic;
import de.digitalcollections.model.api.identifiable.entity.parts.Subtopic;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.IdentifierImpl;
import de.digitalcollections.model.impl.identifiable.entity.TopicImpl;
import de.digitalcollections.model.impl.identifiable.entity.parts.SubtopicImpl;
import de.digitalcollections.model.impl.identifiable.resource.ImageFileResourceImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class TopicRepositoryImpl extends EntityRepositoryImpl<Topic> implements TopicRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(TopicRepositoryImpl.class);

  // select all details shown/needed in single object details page
  private static final String FIND_ONE_BASE_SQL =
      "SELECT t.uuid t_uuid, t.refid t_refId, t.label t_label, t.description t_description,"
          + " t.identifiable_type t_type, t.entity_type t_entityType,"
          + " t.created t_created, t.last_modified t_lastModified,"
          + " t.preview_hints t_previewImageRenderingHints,"
          + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
          + " file.uuid f_uuid, file.filename f_filename, file.mimetype f_mimeType, file.size_in_bytes f_sizeInBytes, file.uri f_uri, file.http_base_url f_httpBaseUrl"
          + " FROM topics as t"
          + " LEFT JOIN identifiers as id on t.uuid = id.identifiable"
          + " LEFT JOIN fileresources_image as file on t.previewfileresource = file.uuid";

  // select only what is shown/needed in paged list (to avoid unnecessary payload/traffic):
  private static final String REDUCED_FIND_ONE_BASE_SQL =
      "SELECT t.uuid t_uuid, t.refid t_refId, t.label t_label, t.description t_description,"
          + " t.identifiable_type t_type, t.entity_type t_entityType,"
          + " t.created t_created, t.last_modified t_lastModified,"
          + " t.preview_hints t_previewImageRenderingHints,"
          + " file.uuid f_uuid, file.filename f_filename, file.mimetype f_mimeType, file.size_in_bytes f_sizeInBytes, file.uri f_uri, file.http_base_url f_httpBaseUrl"
          + " FROM topics as t"
          + " LEFT JOIN fileresources_image as file on t.previewfileresource = file.uuid";

  @Autowired
  public TopicRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository) {
    super(dbi, identifierRepository);
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM topics";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOne().get());
    return count;
  }

  @Override
  public PageResponse<Topic> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder(REDUCED_FIND_ONE_BASE_SQL);
    addPageRequestParams(pageRequest, query);

    List<TopicImpl> result =
        new ArrayList(
            dbi.withHandle(
                h ->
                    h.createQuery(query.toString())
                        .registerRowMapper(BeanMapper.factory(TopicImpl.class, "t"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, TopicImpl>(),
                            (map, rowView) -> {
                              TopicImpl topic =
                                  map.computeIfAbsent(
                                      rowView.getColumn("t_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(TopicImpl.class);
                                      });

                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                topic.setPreviewImage(rowView.getRow(ImageFileResourceImpl.class));
                              }
                              return map;
                            })
                        .values()));

    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public Topic findOne(UUID uuid) {
    String query = FIND_ONE_BASE_SQL + " WHERE t.uuid = :uuid";

    TopicImpl result =
        dbi.withHandle(
                h ->
                    h.createQuery(query)
                        .bind("uuid", uuid)
                        .registerRowMapper(BeanMapper.factory(TopicImpl.class, "t"))
                        .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, TopicImpl>(),
                            (map, rowView) -> {
                              TopicImpl topic =
                                  map.computeIfAbsent(
                                      rowView.getColumn("t_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(TopicImpl.class);
                                      });

                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                topic.setPreviewImage(rowView.getRow(ImageFileResourceImpl.class));
                              }

                              if (rowView.getColumn("id_uuid", UUID.class) != null) {
                                IdentifierImpl identifier = rowView.getRow(IdentifierImpl.class);
                                topic.addIdentifier(identifier);
                              }

                              return map;
                            }))
            .get(uuid);
    if (result != null) {
      // TODO could be replaced with another join in above query...
      result.setSubtopics(TopicRepositoryImpl.this.getSubtopics(result));
    }
    return result;
  }

  @Override
  public Topic findOne(Identifier identifier) {
    if (identifier.getIdentifiable() != null) {
      return findOne(identifier.getIdentifiable());
    }

    String namespace = identifier.getNamespace();
    String identifierId = identifier.getId();

    String query = FIND_ONE_BASE_SQL + " WHERE id.identifier = :id AND id.namespace = :namespace";

    Optional<TopicImpl> result =
        dbi
            .withHandle(
                h ->
                    h.createQuery(query)
                        .bind("id", identifierId)
                        .bind("namespace", namespace)
                        .registerRowMapper(BeanMapper.factory(TopicImpl.class, "t"))
                        .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, TopicImpl>(),
                            (map, rowView) -> {
                              TopicImpl topic =
                                  map.computeIfAbsent(
                                      rowView.getColumn("t_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(TopicImpl.class);
                                      });

                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                topic.setPreviewImage(rowView.getRow(ImageFileResourceImpl.class));
                              }

                              if (rowView.getColumn("id_uuid", UUID.class) != null) {
                                IdentifierImpl dbIdentifier = rowView.getRow(IdentifierImpl.class);
                                topic.addIdentifier(dbIdentifier);
                              }

                              return map;
                            }))
            .values()
            .stream()
            .findFirst();

    Topic topic = result.orElse(null);
    if (topic != null) {
      // TODO could be replaced with another join in above query...
      topic.setSubtopics(TopicRepositoryImpl.this.getSubtopics(topic));
    }
    return topic;
  }

  @Override
  public List<Subtopic> getSubtopics(Topic topic) {
    UUID uuid = topic.getUuid();
    return getSubtopics(uuid);
  }

  @Override
  public List<Subtopic> getSubtopics(UUID uuid) {
    // minimal data required (= identifiable fields) for creating text links/teasers in a list
    String query =
        "SELECT st.uuid st_uuid, st.label st_label, st.description st_description,"
            + " st.identifiable_type st_type,"
            + " st.created st_created, st.last_modified st_lastModified,"
            + " file.uuid f_uuid, file.filename f_filename, file.mimetype f_mimeType, file.size_in_bytes f_sizeInBytes, file.uri f_uri, file.http_base_url f_httpBaseUrl"
            + " FROM subtopics as st INNER JOIN topic_subtopics ts ON st.uuid = ts.subtopic_uuid"
            + " LEFT JOIN fileresources_image as file on st.previewfileresource = file.uuid"
            + " WHERE ts.topic_uuid = :uuid"
            + " ORDER BY ts.sortIndex ASC";

    List<Subtopic> result =
        new ArrayList(
            dbi.withHandle(
                h ->
                    h.createQuery(query)
                        .bind("uuid", uuid)
                        .registerRowMapper(BeanMapper.factory(SubtopicImpl.class, "st"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, SubtopicImpl>(),
                            (map, rowView) -> {
                              SubtopicImpl subtopic =
                                  map.computeIfAbsent(
                                      rowView.getColumn("st_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(SubtopicImpl.class);
                                      });

                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                subtopic.setPreviewImage(
                                    rowView.getRow(ImageFileResourceImpl.class));
                              }
                              return map;
                            })
                        .values()));
    return result;
  }

  @Override
  public Topic save(Topic topic) {
    topic.setUuid(UUID.randomUUID());
    topic.setCreated(LocalDateTime.now());
    topic.setLastModified(LocalDateTime.now());
    // refid is generated as serial, DO NOT SET!
    final UUID previewImageUuid =
        topic.getPreviewImage() == null ? null : topic.getPreviewImage().getUuid();

    String query =
        "INSERT INTO topics("
            + "uuid, label, description, previewfileresource, preview_hints,"
            + " identifiable_type, entity_type,"
            + " created, last_modified"
            + ") VALUES ("
            + ":uuid, :label::JSONB, :description::JSONB, :previewFileResource, :previewImageRenderingHints::JSONB,"
            + " :type, :entityType,"
            + " :created, :lastModified"
            + ")";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(topic)
                .execute());

    // save identifiers
    Set<Identifier> identifiers = topic.getIdentifiers();
    saveIdentifiers(identifiers, topic);

    Topic result = findOne(topic.getUuid());
    return result;
  }

  @Override
  public Topic update(Topic topic) {
    topic.setLastModified(LocalDateTime.now());

    // do not update/left out from statement (not changed since insert):
    // uuid, created, identifiable_type, entity_type, refid
    final UUID previewImageUuid =
        topic.getPreviewImage() == null ? null : topic.getPreviewImage().getUuid();

    String query =
        "UPDATE topics SET"
            + " label=:label::JSONB, description=:description::JSONB,"
            + " previewfileresource=:previewFileResource, preview_hints=:previewImageRenderingHints::JSONB,"
            + " last_modified=:lastModified"
            + " WHERE uuid=:uuid";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(topic)
                .execute());

    // save identifiers
    // as we store the whole list new: delete old entries
    deleteIdentifiers(topic);
    Set<Identifier> identifiers = topic.getIdentifiers();
    saveIdentifiers(identifiers, topic);

    Topic result = findOne(topic.getUuid());
    return result;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[] {"created", "lastModified", "refId"};
  }

  @Override
  protected String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "created":
        return "t.created";
      case "lastModified":
        return "t.last_modified";
      case "refId":
        return "t.refid";
      default:
        return null;
    }
  }
}
