package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
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
import org.jdbi.v3.core.result.RowView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class IdentifiableRepositoryImpl<I extends Identifiable>
    extends AbstractPagingAndSortingRepositoryImpl implements IdentifiableRepository<I> {

  private static final Logger LOGGER = LoggerFactory.getLogger(IdentifiableRepositoryImpl.class);

  // select all details shown/needed in single object details page
  private static final String FIND_ONE_BASE_SQL =
      "SELECT i.uuid i_uuid, i.label i_label, i.description i_description,"
          + " i.identifiable_type i_type,"
          + " i.created i_created, i.last_modified i_last_modified,"
          + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
          + " file.filename f_filename, file.mimetype f_mimetype, file.size_in_bytes f_size_in_bytes, file.uri f_uri"
          + " FROM identifiables as i"
          + " LEFT JOIN identifiers as id on i.uuid = id.identifiable"
          + " LEFT JOIN fileresources_image as file on i.previewfileresource = file.uuid";

  // select only what is shown/needed in paged list (to avoid unnecessary payload/traffic):
  private static final String REDUCED_FIND_ONE_BASE_SQL =
      "SELECT i.uuid i_uuid, i.label i_label, i.description i_description,"
          + " i.identifiable_type i_type,"
          + " i.created i_created, i.last_modified i_lastModified,"
          + " file.uri f_uri, file.filename f_filename"
          + " FROM identifiables as i"
          + " LEFT JOIN fileresources_image as file on i.previewfileresource = file.uuid";

  protected Jdbi dbi;
  protected final IdentifierRepository identifierRepository;

  @Autowired
  public IdentifiableRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository) {
    this.dbi = dbi;
    this.identifierRepository = identifierRepository;
  }

  // this method can be used, if sql prefix of main object columns is not "f"
  protected <T extends Identifiable> LinkedHashMap<UUID, T> addPreviewImage(
      LinkedHashMap<UUID, T> map, RowView rowView, Class<T> clz, String idColumnName) {
    return addPreviewImage(map, rowView, clz, idColumnName, "f_uri");
  }

  // this method must be used, if sql prefix of main object columns is equals "f"
  protected <T extends Identifiable> LinkedHashMap<UUID, T> addPreviewImage(
      LinkedHashMap<UUID, T> map,
      RowView rowView,
      Class<T> clz,
      String idColumnName,
      String previewFileColumnName) {
    T obj =
        map.computeIfAbsent(
            rowView.getColumn(idColumnName, UUID.class), uuid -> rowView.getRow(clz));
    if (rowView.getColumn(previewFileColumnName, String.class) != null) {
      obj.setPreviewImage(rowView.getRow(ImageFileResourceImpl.class));
    }
    return map;
  }

  // this method can be used, if sql prefix of main object columns is not "f"
  protected <T extends Identifiable> LinkedHashMap<UUID, T> addPreviewImageAndIdentifiers(
      LinkedHashMap<UUID, T> map, RowView rowView, Class<T> clz, String idColumnName) {
    return addPreviewImageAndIdentifiers(map, rowView, clz, idColumnName, "f_uri");
  }

  // this method must be used, if sql prefix of main object columns is equals "f"
  protected <T extends Identifiable> LinkedHashMap<UUID, T> addPreviewImageAndIdentifiers(
      LinkedHashMap<UUID, T> map,
      RowView rowView,
      Class<T> clz,
      String idColumnName,
      String previewFileColumnName) {
    T obj =
        map.computeIfAbsent(
            rowView.getColumn(idColumnName, UUID.class), uuid -> rowView.getRow(clz));
    if (rowView.getColumn(previewFileColumnName, String.class) != null) {
      obj.setPreviewImage(rowView.getRow(ImageFileResourceImpl.class));
    }
    if (rowView.getColumn("id_uuid", UUID.class) != null) {
      obj.addIdentifier(rowView.getRow(IdentifierImpl.class));
    }
    return map;
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM identifiables";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOne().get());
    return count;
  }

  protected void deleteIdentifiers(Identifiable identifiable) {
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM identifiers WHERE identifiable = :uuid")
                .bind("uuid", identifiable.getUuid())
                .execute());
  }

  @Override
  public PageResponse<I> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder(REDUCED_FIND_ONE_BASE_SQL);
    addPageRequestParams(pageRequest, query);

    List<Identifiable> result =
        dbi.withHandle(
            h ->
                h.createQuery(query.toString())
                    .registerRowMapper(BeanMapper.factory(IdentifiableImpl.class, "i"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                    .reduceRows(
                        new LinkedHashMap<UUID, IdentifiableImpl>(),
                        (map, rowView) ->
                            addPreviewImage(map, rowView, IdentifiableImpl.class, "i_uuid"))
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
    String query = FIND_ONE_BASE_SQL + " WHERE i.uuid = :uuid";

    Optional<IdentifiableImpl> resultOpt =
        dbi.withHandle(
            h ->
                h.createQuery(query).bind("uuid", uuid)
                    .registerRowMapper(BeanMapper.factory(IdentifiableImpl.class, "i"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                    .reduceRows(
                        new LinkedHashMap<UUID, IdentifiableImpl>(),
                        (map, rowView) ->
                            addPreviewImageAndIdentifiers(
                                map, rowView, IdentifiableImpl.class, "i_uuid"))
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

    String query = FIND_ONE_BASE_SQL + " WHERE id.identifier = :id AND id.namespace = :namespace";

    Optional<IdentifiableImpl> resultOpt =
        dbi.withHandle(
            h ->
                h.createQuery(query).bind("id", identifierId).bind("namespace", namespace)
                    .registerRowMapper(BeanMapper.factory(IdentifiableImpl.class, "i"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                    .reduceRows(
                        new LinkedHashMap<UUID, IdentifiableImpl>(),
                        (map, rowView) ->
                            addPreviewImageAndIdentifiers(
                                map, rowView, IdentifiableImpl.class, "i_uuid"))
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
    return new String[] {"i.created", "i.last_modified", "i.identifiable_type"};
  }

  @Override
  public I save(I identifiable) {
    throw new UnsupportedOperationException(
        "use save of specific/inherited identifiable repository");
  }

  protected void saveIdentifiers(List<Identifier> identifiers, Identifiable identifiable) {
    // we assume that identifiers (unique to object) are new (existing ones were deleted before
    // (e.g. see update))
    if (identifiers != null) {
      for (Identifier identifier : identifiers) {
        identifier.setIdentifiable(identifiable.getUuid());
        identifierRepository.save(identifier);
      }
    }
  }

  @Override
  public I update(I identifiable) {
    throw new UnsupportedOperationException(
        "use update of specific/inherited identifiable repository");
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
