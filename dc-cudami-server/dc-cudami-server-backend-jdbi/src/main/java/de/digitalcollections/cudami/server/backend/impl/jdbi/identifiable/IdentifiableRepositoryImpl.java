package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.AbstractPagingAndSortingRepositoryImpl;
import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.IdentifiableImpl;
import de.digitalcollections.model.impl.identifiable.IdentifierImpl;
import de.digitalcollections.model.impl.identifiable.resource.ImageFileResourceImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class IdentifiableRepositoryImpl<I extends Identifiable>
    extends AbstractPagingAndSortingRepositoryImpl implements IdentifiableRepository<I> {

  private static final Logger LOGGER = LoggerFactory.getLogger(IdentifiableRepositoryImpl.class);

  protected Jdbi dbi;

  @Autowired
  public IdentifiableRepositoryImpl(Jdbi dbi) {
    this.dbi = dbi;
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM identifiables";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOne().get());
    return count;
  }

  @Override
  public PageResponse<I> find(PageRequest pageRequest) {
    // select only what is shown/needed in paged list (commented some additional prepared fields):
    StringBuilder query =
        new StringBuilder(
            "SELECT i.uuid i_uuid, i.label i_label, i.description i_description,"
                + " i.identifiable_type i_type,"
                + " i.created i_created, i.last_modified i_lastModified,"
                //      + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace
                // id_namespace, id.identifier id_id,"
                + " file.uri f_uri, file.filename f_filename"
                + " FROM identifiables as i"
                //      + " LEFT JOIN identifiers as id on i.uuid = id.identifiable"
                + " LEFT JOIN fileresources_image as file on i.previewfileresource = file.uuid");
    addPageRequestParams(pageRequest, query);

    List<Identifiable> result =
        dbi.withHandle(
            h ->
                h.createQuery(query.toString())
                    .registerRowMapper(BeanMapper.factory(IdentifiableImpl.class, "i"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                    .reduceRows(
                        new LinkedHashMap<UUID, Identifiable>(),
                        (map, rowView) -> {
                          Identifiable identifiable =
                              map.computeIfAbsent(
                                  rowView.getColumn("i_uuid", UUID.class),
                                  uuid -> rowView.getRow(IdentifiableImpl.class));
                          if (rowView.getColumn("f_uri", String.class) != null) {
                            identifiable.setPreviewImage(
                                rowView.getRow(ImageFileResourceImpl.class));
                          }
                          return map;
                        })
                    .values().stream()
                    .collect(Collectors.toList()));
    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public List<I> find(String searchTerm, int maxResults) {
    // TODO: think about using GIN (but seems not to support like queries...)
    // see: https://bitnine.net/blog-postgresql/postgresql-internals-jsonb-type-and-its-indexes/
    // see: https://www.postgresql.org/docs/10/datatype-json.html
    StringBuilder query =
        new StringBuilder(
            "WITH flattened AS (SELECT uuid, label, description, identifiable_type, jsonb_array_elements(label#>'{translations}')->>'text' AS text FROM identifiables)");
    query.append(
        " SELECT uuid, label, description, identifiable_type FROM flattened WHERE text ILIKE '%' || :searchTerm || '%'");
    query.append(" LIMIT :maxResults");

    List<I> result =
        dbi.withHandle(
            h ->
                h.createQuery(query.toString()).bind("searchTerm", searchTerm)
                    .bind("maxResults", maxResults).mapToBean(IdentifiableImpl.class).stream()
                    .map(s -> (I) s)
                    .collect(Collectors.toList()));
    return result;
  }

  @Override
  public I findOne(UUID uuid) {
    String query =
        "SELECT i.uuid i_uuid, i.label i_label, i.description i_description,"
            + " i.identifiable_type i_type,"
            + " i.created i_created, i.last_modified i_last_modified,"
            + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
            + " file.filename f_filename, file.mimetype f_mimetype, file.size_in_bytes f_size_in_bytes, file.uri f_uri"
            + " FROM identifiables as i"
            + " LEFT JOIN identifiers as id on i.uuid = id.identifiable"
            + " LEFT JOIN fileresources_image as file on i.previewfileresource = file.uuid"
            + " WHERE i.uuid = :uuid";

    Optional<Identifiable> resultOpt =
        dbi.withHandle(
            h ->
                h.createQuery(query).bind("uuid", uuid)
                    .registerRowMapper(BeanMapper.factory(IdentifiableImpl.class, "i"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                    .reduceRows(
                        new LinkedHashMap<UUID, Identifiable>(),
                        (map, rowView) -> {
                          Identifiable identifiable =
                              map.computeIfAbsent(
                                  rowView.getColumn("i_uuid", UUID.class),
                                  id -> rowView.getRow(IdentifiableImpl.class));
                          if (rowView.getColumn("id_uuid", UUID.class) != null) {
                            identifiable.addIdentifier(rowView.getRow(IdentifierImpl.class));
                          }
                          if (rowView.getColumn("f_uri", String.class) != null) {
                            identifiable.setPreviewImage(
                                rowView.getRow(ImageFileResourceImpl.class));
                          }
                          return map;
                        })
                    .values().stream()
                    .findFirst());
    if (!resultOpt.isPresent()) {
      return null;
    }
    return (I) resultOpt.get();
  }

  @Override
  public I findOne(Identifier identifier) {
    if (identifier.getIdentifiable() != null) {
      return findOne(identifier.getIdentifiable());
    }

    String namespace = identifier.getNamespace();
    String identifierId = identifier.getId();

    String query =
        "SELECT i.uuid i_uuid, i.label i_label, i.description i_description,"
            + " i.identifiable_type i_type,"
            + " i.created i_created, i.last_modified i_last_modified,"
            + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
            + " file.filename f_filename, file.mimetype f_mimetype, file.size_in_bytes f_size_in_bytes, file.uri f_uri"
            + " FROM identifiables as i"
            + " LEFT JOIN identifiers as id on i.uuid = id.identifiable"
            + " LEFT JOIN fileresources_image as file on i.previewfileresource = file.uuid"
            + " WHERE id.identifier = :id AND id.namespace = :namespace";

    Optional<Identifiable> resultOpt =
        dbi.withHandle(
            h ->
                h.createQuery(query).bind("id", identifierId).bind("namespace", namespace)
                    .registerRowMapper(BeanMapper.factory(IdentifiableImpl.class, "i"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                    .reduceRows(
                        new LinkedHashMap<UUID, Identifiable>(),
                        (map, rowView) -> {
                          Identifiable identifiable =
                              map.computeIfAbsent(
                                  rowView.getColumn("i_uuid", UUID.class),
                                  id -> rowView.getRow(IdentifiableImpl.class));
                          if (rowView.getColumn("id_uuid", UUID.class) != null) {
                            identifiable.addIdentifier(rowView.getRow(IdentifierImpl.class));
                          }
                          if (rowView.getColumn("f_uri", String.class) != null) {
                            identifiable.setPreviewImage(
                                rowView.getRow(ImageFileResourceImpl.class));
                          }
                          return map;
                        })
                    .values().stream()
                    .findFirst());
    if (!resultOpt.isPresent()) {
      return null;
    }
    return (I) resultOpt.get();
  }

  @Override
  public I findOneByIdentifier(String namespace, String id) {
    return findOne(new IdentifierImpl(null, namespace, id));
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[] {"created", "type", "last_modified"};
  }

  @Override
  public I save(I identifiable) {
    throw new UnsupportedOperationException(
        "use save of specific/inherited identifiable repository");
    //    identifiable.setUuid(UUID.randomUUID());
    //    identifiable.setCreated(LocalDateTime.now());
    //    identifiable.setLastModified(LocalDateTime.now());
    //
    //    IdentifiableImpl result = dbi.withHandle(h -> h
    //        .createQuery("INSERT INTO identifiables(created, description, identifiable_type,
    // label, last_modified, uuid) VALUES (:created, :description::JSONB, :type, :label::JSONB,
    // :lastModified, :uuid) RETURNING *")
    //        .bindBean(identifiable)
    //        .mapToBean(IdentifiableImpl.class)
    //        .findOne().orElse(null));
    //    return (I) result;
  }

  @Override
  public I update(I identifiable) {
    throw new UnsupportedOperationException(
        "use update of specific/inherited identifiable repository");
    //    identifiable.setLastModified(LocalDateTime.now());
    //
    //    // do not update/left out from statement: created, uuid
    //    IdentifiableImpl result = dbi.withHandle(h -> h
    //        .createQuery("UPDATE identifiables SET description=:description::JSONB,
    // identifiable_type=:type, label=:label::JSONB, last_modified=:lastModified WHERE uuid=:uuid
    // RETURNING *")
    //        .bindBean(identifiable)
    //        .mapToBean(IdentifiableImpl.class)
    //        .findOne().orElse(null));
    //    return (I) result;
  }

  protected Integer selectNextSortIndexForParentChildren(
      Jdbi dbi, String tableName, String columNameParentUuid, UUID parentUuid) {
    // first child: max gets no results (= null)):
    Integer sortIndex =
        dbi.withHandle(
            (Handle h) ->
                h.createQuery(
                        "SELECT MAX(sortIndex) + 1 FROM "
                            + tableName
                            + " WHERE "
                            + columNameParentUuid
                            + " = :parent_uuid")
                    .bind("parent_uuid", parentUuid)
                    .mapTo(Integer.class)
                    .findOne()
                    .orElse(null));
    if (sortIndex == null) {
      sortIndex = 0;
    }
    final Integer sortIndexDb = sortIndex;
    return sortIndexDb;
  }

  protected int getIndex(LinkedHashSet<? extends Identifiable> list, Identifiable identifiable) {
    int pos = -1;
    for (Iterator iterator = list.iterator(); iterator.hasNext(); ) {
      pos = pos + 1;
      Identifiable idf = (Identifiable) iterator.next();
      if (idf.getUuid().equals(identifiable.getUuid())) {
        return pos;
      }
    }
    return -1;
  }
}
