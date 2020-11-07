package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.work;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work.WorkRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.agent.Agent;
import de.digitalcollections.model.api.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.api.identifiable.entity.agent.Family;
import de.digitalcollections.model.api.identifiable.entity.agent.Person;
import de.digitalcollections.model.api.identifiable.entity.enums.EntityType;
import de.digitalcollections.model.api.identifiable.entity.work.Item;
import de.digitalcollections.model.api.identifiable.entity.work.Work;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.IdentifierImpl;
import de.digitalcollections.model.impl.identifiable.entity.EntityImpl;
import de.digitalcollections.model.impl.identifiable.entity.agent.CorporateBodyImpl;
import de.digitalcollections.model.impl.identifiable.entity.agent.FamilyImpl;
import de.digitalcollections.model.impl.identifiable.entity.agent.PersonImpl;
import de.digitalcollections.model.impl.identifiable.entity.work.ItemImpl;
import de.digitalcollections.model.impl.identifiable.entity.work.WorkImpl;
import de.digitalcollections.model.impl.identifiable.resource.ImageFileResourceImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
public class WorkRepositoryImpl extends IdentifiableRepositoryImpl<Work> implements WorkRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkRepositoryImpl.class);

  // select all details shown/needed in single object details page
  private static final String FIND_ONE_BASE_SQL =
      "SELECT w.uuid w_uuid, w.refid w_refId, w.label w_label, w.description w_description,"
          + " w.identifiable_type w_type, w.entity_type w_entityType,"
          + " w.created w_created, w.last_modified w_lastModified,"
          + " w.date_published w_datePublished, w.timevalue_published w_timeValuePublished,"
          + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
          + " file.uuid f_uuid, file.filename f_filename, file.mimetype f_mimetype, file.size_in_bytes f_size_in_bytes, file.uri f_uri"
          + " FROM works as w"
          + " LEFT JOIN identifiers as id on w.uuid = id.identifiable"
          + " LEFT JOIN fileresources_image as file on w.previewfileresource = file.uuid";

  // select only what is shown/needed in paged list (to avoid unnecessary payload/traffic):
  private static final String REDUCED_FIND_ONE_BASE_SQL =
      "SELECT w.uuid w_uuid, w.refid w_refId, w.label w_label, w.description w_description,"
          + " w.identifiable_type w_type, w.entity_type w_entityType,"
          + " w.created w_created, w.last_modified w_lastModified,"
          + " w.date_published w_datePublished, w.timevalue_published w_timeValuePublished,"
          + " file.uuid f_uuid, file.filename f_filename, file.uri f_uri"
          + " FROM works as w"
          + " LEFT JOIN fileresources_image as file on w.previewfileresource = file.uuid";

  @Autowired
  public WorkRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository) {
    super(dbi, identifierRepository);
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM works";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOne().get());
    return count;
  }

  @Override
  public PageResponse<Work> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder(REDUCED_FIND_ONE_BASE_SQL);
    addPageRequestParams(pageRequest, query);

    List<WorkImpl> result =
        new ArrayList(
            dbi.withHandle(
                h ->
                    h.createQuery(query.toString())
                        .registerRowMapper(BeanMapper.factory(WorkImpl.class, "w"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, WorkImpl>(),
                            (map, rowView) -> {
                              WorkImpl work =
                                  map.computeIfAbsent(
                                      rowView.getColumn("w_uuid", UUID.class),
                                      uuid -> rowView.getRow(WorkImpl.class));
                              if (rowView.getColumn("f_uuid", String.class) != null) {
                                work.setPreviewImage(rowView.getRow(ImageFileResourceImpl.class));
                              }
                              return map;
                            })
                        .values()));

    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public PageResponse<Work> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial) {
    // select only what is shown/needed in alphabetical sorted paged list:
    StringBuilder query =
        new StringBuilder(
            "SELECT w.uuid w_uuid, w.refid w_refId, w.label w_label, w.description w_description,"
                //      + " w.identifiable_type w_type, w.entity_type w_entityType,
                // w.geolocation_type w_geoLocationType,"
                //      + " w.created w_created, w.last_modified w_last_modified,"
                + " w.date_published w_datePublished, w.timevalue_published w_timeValuePublished,"
                //      + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace
                // id_namespace, id.identifier id_id,"
                + " file.uuid f_uuid, file.uri f_uri, file.filename f_filename"
                + " FROM works as w"
                //      + " LEFT JOIN identifiers as id on w.uuid = id.identifiable"
                + " LEFT JOIN fileresources_image as file on w.previewfileresource = file.uuid"
                + " WHERE w.label ->> :language IS NOT null AND w.label ->> :language ILIKE :initial || '%'"
                + " ORDER BY w.label ->> :language");
    addPageRequestParams(pageRequest, query);

    List<Work> result =
        new ArrayList(
            dbi.withHandle(
                h ->
                    h.createQuery(query.toString())
                        .bind("language", language)
                        .bind("initial", initial)
                        .registerRowMapper(BeanMapper.factory(WorkImpl.class, "w"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, Work>(),
                            (map, rowView) -> {
                              Work work =
                                  map.computeIfAbsent(
                                      rowView.getColumn("w_uuid", UUID.class),
                                      uuid -> rowView.getRow(WorkImpl.class));
                              if (rowView.getColumn("f_uuid", String.class) != null) {
                                work.setPreviewImage(rowView.getRow(ImageFileResourceImpl.class));
                              }
                              return map;
                            })
                        .values()));

    String countQuery =
        "SELECT count(*) FROM works as w"
            + " WHERE w.label ->> :language IS NOT null AND w.label ->> :language ILIKE :initial || '%'";
    long total =
        dbi.withHandle(
            h ->
                h.createQuery(countQuery)
                    .bind("language", language)
                    .bind("initial", initial)
                    .mapTo(Long.class)
                    .findOne()
                    .get());

    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public Work findOne(UUID uuid) {
    String query = FIND_ONE_BASE_SQL + " WHERE w.uuid = :uuid";

    WorkImpl result =
        dbi.withHandle(
                h ->
                    h.createQuery(query)
                        .bind("uuid", uuid)
                        .registerRowMapper(BeanMapper.factory(WorkImpl.class, "w"))
                        .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, WorkImpl>(),
                            (map, rowView) -> {
                              WorkImpl work =
                                  map.computeIfAbsent(
                                      rowView.getColumn("w_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(WorkImpl.class);
                                      });

                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                work.setPreviewImage(rowView.getRow(ImageFileResourceImpl.class));
                              }

                              if (rowView.getColumn("id_uuid", UUID.class) != null) {
                                IdentifierImpl identifier = rowView.getRow(IdentifierImpl.class);
                                work.addIdentifier(identifier);
                              }

                              return map;
                            }))
            .get(uuid);

    if (result != null) {
      List<Agent> creators = getCreators(uuid);
      result.setCreators(creators);
    }
    return result;
  }

  @Override
  public Work findOne(Identifier identifier) {
    if (identifier.getIdentifiable() != null) {
      return findOne(identifier.getIdentifiable());
    }

    String namespace = identifier.getNamespace();
    String identifierId = identifier.getId();

    String query = FIND_ONE_BASE_SQL + " WHERE id.identifier = :id AND id.namespace = :namespace";

    Optional<WorkImpl> result =
        dbi
            .withHandle(
                h ->
                    h.createQuery(query)
                        .bind("id", identifierId)
                        .bind("namespace", namespace)
                        .registerRowMapper(BeanMapper.factory(WorkImpl.class, "w"))
                        .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, WorkImpl>(),
                            (map, rowView) -> {
                              WorkImpl work =
                                  map.computeIfAbsent(
                                      rowView.getColumn("w_uuid", UUID.class),
                                      fn -> {
                                        return rowView.getRow(WorkImpl.class);
                                      });

                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                work.setPreviewImage(rowView.getRow(ImageFileResourceImpl.class));
                              }

                              if (rowView.getColumn("id_uuid", UUID.class) != null) {
                                IdentifierImpl dbIdentifier = rowView.getRow(IdentifierImpl.class);
                                work.addIdentifier(dbIdentifier);
                              }

                              return map;
                            }))
            .values()
            .stream()
            .findFirst();
    WorkImpl work = result.orElse(null);

    if (work != null) {
      work.setCreators(getCreators(work.getUuid()));
    }
    return work;
  }

  @Override
  public Work findOneByIdentifier(String namespace, String id) {
    return findOne(new IdentifierImpl(null, namespace, id));
  }

  @Override
  public Work save(Work work) {
    work.setUuid(UUID.randomUUID());
    work.setCreated(LocalDateTime.now());
    work.setLastModified(LocalDateTime.now());
    // refid is generated as serial, DO NOT SET!
    final UUID previewImageUuid =
        work.getPreviewImage() == null ? null : work.getPreviewImage().getUuid();

    String query =
        "INSERT INTO works("
            + "uuid, label, description, previewfileresource,"
            + " identifiable_type, entity_type,"
            + " created, last_modified,"
            + " date_published , timevalue_published"
            + ") VALUES ("
            + ":uuid, :label::JSONB, :description::JSONB, :previewFileResource,"
            + " :type, :entityType,"
            + " :created, :lastModified,"
            + " :datePublished, :timeValuePublished::JSONB"
            + ")";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(work)
                .execute());

    // save identifiers
    Set<Identifier> identifiers = work.getIdentifiers();
    saveIdentifiers(identifiers, work);

    // save creators
    List<Agent> creators = work.getCreators();
    saveCreatorsList(work, creators);

    Work result = findOne(work.getUuid());
    return result;
  }

  @Override
  public Work update(Work work) {
    work.setLastModified(LocalDateTime.now());
    // do not update/left out from statement (not changed since insert):
    // uuid, created, identifiable_type, entity_type, refid
    final UUID previewImageUuid =
        work.getPreviewImage() == null ? null : work.getPreviewImage().getUuid();

    String query =
        "UPDATE works SET"
            + " label=:label::JSONB, description=:description::JSONB, previewfileresource=:previewFileResource,"
            + " last_modified=:lastModified,"
            + " date_published=:datePublished , timevalue_published=:timeValuePublished::JSONB"
            + " WHERE uuid=:uuid";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(work)
                .execute());

    // save identifiers
    // as we store the whole list new: delete old entries
    deleteIdentifiers(work);
    Set<Identifier> identifiers = work.getIdentifiers();
    saveIdentifiers(identifiers, work);

    // save creators
    List<Agent> creators = work.getCreators();
    saveCreatorsList(work, creators);

    Work result = findOne(work.getUuid());
    return result;
  }

  @Override
  public List<Agent> getCreators(UUID workUuid) {
    String query =
        "SELECT e.uuid e_uuid, e.label e_label, e.refid e_refId, e.entity_type e_entityType,"
            + " e.created e_created, e.last_modified e_lastModified,"
            + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
            + " file.uuid pf_uuid, file.filename pf_filename, file.mimetype pf_mimetype, file.size_in_bytes pf_size_in_bytes, file.uri pf_uri"
            + " FROM entities as e"
            + " LEFT JOIN identifiers as id on e.uuid = id.identifiable"
            + " LEFT JOIN fileresources_image as file on e.previewfileresource = file.uuid"
            + " LEFT JOIN work_creators as wc on e.uuid = wc.agent_uuid"
            + " WHERE wc.work_uuid = :uuid"
            + " ORDER BY wc.sortIndex ASC";

    List<Agent> result =
        dbi.withHandle(
            h ->
                h
                    .createQuery(query)
                    .bind("uuid", workUuid)
                    .registerRowMapper(BeanMapper.factory(EntityImpl.class, "e"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pf"))
                    .reduceRows(
                        new LinkedHashMap<UUID, Entity>(),
                        (map, rowView) -> {
                          Entity entity =
                              map.computeIfAbsent(
                                  rowView.getColumn("e_uuid", UUID.class),
                                  fn -> {
                                    return rowView.getRow(EntityImpl.class);
                                  });

                          if (rowView.getColumn("pf_uuid", UUID.class) != null) {
                            entity.setPreviewImage(rowView.getRow(ImageFileResourceImpl.class));
                          }

                          if (rowView.getColumn("id_uuid", UUID.class) != null) {
                            IdentifierImpl dbIdentifier = rowView.getRow(IdentifierImpl.class);
                            entity.addIdentifier(dbIdentifier);
                          }
                          return map;
                        })
                    .values()
                    .stream()
                    .map(
                        (entity) -> {
                          EntityType entityType = entity.getEntityType();
                          switch (entityType) {
                            case CORPORATE_BODY:
                              CorporateBody corporateBody = new CorporateBodyImpl();
                              corporateBody.setLabel(entity.getLabel());
                              corporateBody.setRefId(entity.getRefId());
                              corporateBody.setUuid(entity.getUuid());
                              return corporateBody;
                            case FAMILY:
                              Family family = new FamilyImpl();
                              family.setLabel(entity.getLabel());
                              family.setRefId(entity.getRefId());
                              family.setUuid(entity.getUuid());
                              return family;
                            case PERSON:
                              Person person = new PersonImpl();
                              person.setLabel(entity.getLabel());
                              person.setRefId(entity.getRefId());
                              person.setUuid(entity.getUuid());
                              return person;
                            default:
                              return null;
                          }
                        })
                    .collect(Collectors.toList()));
    return result;
  }

  @Override
  public List<Item> getItems(UUID workUuid) {
    String query =
        "SELECT e.uuid e_uuid, e.label e_label, e.refid e_refId,"
            + " e.created e_created, e.last_modified e_lastModified,"
            + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
            + " file.uuid pf_uuid, file.filename pf_filename, file.mimetype pf_mimetype, file.size_in_bytes pf_size_in_bytes, file.uri pf_uri"
            + " FROM entities as e"
            + " LEFT JOIN identifiers as id on e.uuid = id.identifiable"
            + " LEFT JOIN fileresources_image as file on e.previewfileresource = file.uuid"
            + " LEFT JOIN item_works as iw on e.uuid = iw.item_uuid"
            + " WHERE iw.work_uuid = :uuid"
            + " ORDER BY iw.sortIndex ASC";

    List<Item> result =
        dbi.withHandle(
            h ->
                h
                    .createQuery(query)
                    .bind("uuid", workUuid)
                    .registerRowMapper(BeanMapper.factory(EntityImpl.class, "e"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pf"))
                    .reduceRows(
                        new LinkedHashMap<UUID, Entity>(),
                        (map, rowView) -> {
                          Entity entity =
                              map.computeIfAbsent(
                                  rowView.getColumn("e_uuid", UUID.class),
                                  fn -> {
                                    return rowView.getRow(EntityImpl.class);
                                  });

                          if (rowView.getColumn("pf_uuid", UUID.class) != null) {
                            entity.setPreviewImage(rowView.getRow(ImageFileResourceImpl.class));
                          }

                          if (rowView.getColumn("id_uuid", UUID.class) != null) {
                            IdentifierImpl dbIdentifier = rowView.getRow(IdentifierImpl.class);
                            entity.addIdentifier(dbIdentifier);
                          }
                          return map;
                        })
                    .values()
                    .stream()
                    .map(
                        (entity) -> {
                          Item item = new ItemImpl();
                          item.setPreviewImage(entity.getPreviewImage());
                          item.setLabel(entity.getLabel());
                          item.setRefId(entity.getRefId());
                          item.setUuid(entity.getUuid());
                          return item;
                        })
                    .collect(Collectors.toList()));
    return result;
  }

  private void saveCreatorsList(Work work, List<Agent> creators) {
    UUID workUuid = work.getUuid();

    // as we store the whole list new: delete old entries
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM work_creators WHERE work_uuid = :uuid")
                .bind("uuid", workUuid)
                .execute());

    if (creators != null) {
      // second: save relations
      dbi.useHandle(
          handle -> {
            PreparedBatch preparedBatch =
                handle.prepareBatch(
                    "INSERT INTO work_creators(work_uuid, agent_uuid, sortIndex) VALUES(:uuid, :agentUuid, :sortIndex)");
            for (Agent agent : creators) {
              preparedBatch
                  .bind("uuid", workUuid)
                  .bind("agentUuid", agent.getUuid())
                  .bind("sortIndex", getIndex(creators, agent))
                  .add();
            }
            preparedBatch.execute();
          });
    }
  }

  protected int getIndex(Set<? extends Identifiable> list, Identifiable identifiable) {
    int pos = -1;
    for (Identifiable idf : list) {
      pos += 1;
      if (idf.getUuid().equals(identifiable.getUuid())) {
        return pos;
      }
    }
    return -1;
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
        return "w.created";
      case "lastModified":
        return "w.last_modified";
      case "refId":
        return "w.refid";
      default:
        return null;
    }
  }
}
