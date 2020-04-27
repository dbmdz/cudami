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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

  // select all details shown/needed in single object details page
  private static final String FIND_ONE_BASE_SQL
          = "SELECT i.uuid i_uuid, i.label i_label, i.description i_description,"
          + " i.identifiable_type i_type,"
          + " i.created i_created, i.last_modified i_last_modified,"
          + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
          + " file.uuid f_uuid, file.filename f_filename, file.mimetype f_mimetype, file.size_in_bytes f_size_in_bytes, file.uri f_uri"
          + " FROM identifiables as i"
          + " LEFT JOIN identifiers as id on i.uuid = id.identifiable"
          + " LEFT JOIN fileresources_image as file on i.previewfileresource = file.uuid";

  // select only what is shown/needed in paged list (to avoid unnecessary payload/traffic):
  private static final String REDUCED_FIND_ONE_BASE_SQL
          = "SELECT i.uuid i_uuid, i.label i_label, i.description i_description,"
          + " i.identifiable_type i_type,"
          + " i.created i_created, i.last_modified i_lastModified,"
          + " file.uuid f_uuid, file.uri f_uri, file.filename f_filename"
          + " FROM identifiables as i"
          + " LEFT JOIN fileresources_image as file on i.previewfileresource = file.uuid";

  protected Jdbi dbi;
  protected final IdentifierRepository identifierRepository;

  @Autowired
  public IdentifiableRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository) {
    this.dbi = dbi;
    this.identifierRepository = identifierRepository;
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM identifiables";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOne().get());
    return count;
  }

  protected void deleteIdentifiers(Identifiable identifiable) {
    dbi.withHandle(
            h
            -> h.createUpdate("DELETE FROM identifiers WHERE identifiable = :uuid")
                    .bind("uuid", identifiable.getUuid())
                    .execute());
  }

  @Override
  public PageResponse<I> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder(REDUCED_FIND_ONE_BASE_SQL);
    addPageRequestParams(pageRequest, query);

    List<Identifiable> result
            = new ArrayList(
                    dbi.withHandle(
                            h
                            -> h.createQuery(query.toString())
                                    .registerRowMapper(BeanMapper.factory(IdentifiableImpl.class, "i"))
                                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                                    .reduceRows(
                                            new LinkedHashMap<UUID, IdentifiableImpl>(),
                                            (map, rowView) -> {
                                              IdentifiableImpl identifiable
                                              = map.computeIfAbsent(
                                                      rowView.getColumn("i_uuid", UUID.class),
                                                      fn -> {
                                                        return rowView.getRow(IdentifiableImpl.class);
                                                      });

                                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                                identifiable.setPreviewImage(
                                                        rowView.getRow(ImageFileResourceImpl.class));
                                              }
                                              return map;
                                            })
                                    .values()));
    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public List<I> find(String searchTerm, int maxResults) {
    // TODO: think about using GIN (but seems not to support like queries...)
    // see: https://bitnine.net/blog-postgresql/postgresql-internals-jsonb-type-and-its-indexes/
    // see: https://www.postgresql.org/docs/10/datatype-json.html
    StringBuilder query
            = new StringBuilder(
                    "SELECT uuid, label, description, identifiable_type FROM identifiables WHERE (label::text) ILIKE '%' || :searchTerm || '%'");
    query.append(" LIMIT :maxResults");

    List<I> result
            = dbi.withHandle(
                    h
                    -> h.createQuery(query.toString()).bind("searchTerm", searchTerm)
                            .bind("maxResults", maxResults).mapToBean(IdentifiableImpl.class).stream()
                            .map(s -> (I) s)
                            .collect(Collectors.toList()));
    return result;
  }

  @Override
  public I findOne(UUID uuid) {
    String query = FIND_ONE_BASE_SQL + " WHERE i.uuid = :uuid";

    IdentifiableImpl result
            = dbi.withHandle(
                    h
                    -> h.createQuery(query)
                            .bind("uuid", uuid)
                            .registerRowMapper(BeanMapper.factory(IdentifiableImpl.class, "i"))
                            .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                            .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                            .reduceRows(
                                    new LinkedHashMap<UUID, IdentifiableImpl>(),
                                    (map, rowView) -> {
                                      IdentifiableImpl identifiable
                                      = map.computeIfAbsent(
                                              rowView.getColumn("i_uuid", UUID.class),
                                              fn -> {
                                                return rowView.getRow(IdentifiableImpl.class);
                                              });

                                      if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                        identifiable.setPreviewImage(
                                                rowView.getRow(ImageFileResourceImpl.class));
                                      }

                                      if (rowView.getColumn("id_uuid", UUID.class) != null) {
                                        IdentifierImpl identifier = rowView.getRow(IdentifierImpl.class);
                                        identifiable.addIdentifier(identifier);
                                      }

                                      return map;
                                    }))
                    .get(uuid);
    return (I) result;
  }

  @Override
  public I findOne(Identifier identifier) {
    if (identifier.getIdentifiable() != null) {
      return findOne(identifier.getIdentifiable());
    }

    String namespace = identifier.getNamespace();
    String identifierId = identifier.getId();

    String query = FIND_ONE_BASE_SQL + " WHERE id.identifier = :id AND id.namespace = :namespace";

    Optional<IdentifiableImpl> result
            = dbi
                    .withHandle(
                            h
                            -> h.createQuery(query)
                                    .bind("id", identifierId)
                                    .bind("namespace", namespace)
                                    .registerRowMapper(BeanMapper.factory(IdentifiableImpl.class, "i"))
                                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                                    .reduceRows(
                                            new LinkedHashMap<UUID, IdentifiableImpl>(),
                                            (map, rowView) -> {
                                              IdentifiableImpl identifiable
                                              = map.computeIfAbsent(
                                                      rowView.getColumn("i_uuid", UUID.class),
                                                      fn -> {
                                                        return rowView.getRow(IdentifiableImpl.class);
                                                      });

                                              if (rowView.getColumn("f_uuid", UUID.class) != null) {
                                                identifiable.setPreviewImage(
                                                        rowView.getRow(ImageFileResourceImpl.class));
                                              }

                                              if (rowView.getColumn("id_uuid", UUID.class) != null) {
                                                IdentifierImpl dbIdentifier = rowView.getRow(IdentifierImpl.class);
                                                identifiable.addIdentifier(dbIdentifier);
                                              }

                                              return map;
                                            }))
                    .values().stream()
                    .findFirst();
    return (I) result.orElse(null);
  }

  @Override
  public I findOneByIdentifier(String namespace, String id) {
    return findOne(new IdentifierImpl(null, namespace, id));
  }

  @Override
  public I save(I identifiable) {
    throw new UnsupportedOperationException(
            "use save of specific/inherited identifiable repository");
  }

  protected void saveIdentifiers(Set<Identifier> identifiers, Identifiable identifiable) {
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
    Integer sortIndex
            = dbi.withHandle(
                    (Handle h)
                    -> h.createQuery(
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
      return 0;
    }
    return sortIndex;
  }

  protected int getIndex(List<? extends Identifiable> list, Identifiable identifiable) {
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
    return new String[]{getColumnName("created"), getColumnName("lastModified"), getColumnName("type")};
  }

  @Override
  protected String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "created":
        return "i.created";
      case "lastModified":
        return "i.last_modified";
      case "type":
        return "i.identifiable_type";
      default:
        return null;
    }
  }
}
