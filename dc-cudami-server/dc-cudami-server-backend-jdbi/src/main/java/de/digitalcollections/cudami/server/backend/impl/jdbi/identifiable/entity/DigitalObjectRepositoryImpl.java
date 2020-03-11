package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.DigitalObjectRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.FileResourceMetadataRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.IdentifiableAggregator;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.IdentifierImpl;
import de.digitalcollections.model.impl.identifiable.entity.DigitalObjectImpl;
import de.digitalcollections.model.impl.identifiable.resource.FileResourceImpl;
import de.digitalcollections.model.impl.identifiable.resource.ImageFileResourceImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
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
public class DigitalObjectRepositoryImpl extends EntityRepositoryImpl<DigitalObject>
    implements DigitalObjectRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(DigitalObjectRepositoryImpl.class);

  // select all details shown/needed in single object details page
  private static final String FIND_ONE_BASE_SQL =
      "SELECT d.uuid d_uuid, d.refid d_refId, d.label d_label, d.description d_description,"
          + " d.identifiable_type d_type, d.entity_type d_entityType,"
          + " d.created d_created, d.last_modified d_lastModified,"
          // TODO: add d.license d_license, d.version d_version, when features added
          + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
          + " file.uuid f_uuid, file.filename f_filename, file.mimetype f_mimetype, file.size_in_bytes f_sizeInBytes, file.uri f_uri,"
          // related file resources
          + " fr.uuid fr_uuid, fr.filename fr_filename, fr.mimetype fr_mimetype, fr.size_in_bytes fr_sizeInBytes, fr.uri fr_uri"
          + " FROM digitalobjects as d"
          + " LEFT JOIN identifiers as id on d.uuid = id.identifiable"
          + " LEFT JOIN fileresources_image as file on d.previewfileresource = file.uuid"
          + " LEFT JOIN digitalobject_fileresources as df on d.uuid = df.digitalobject_uuid"
          + " LEFT JOIN fileresources as fr on fr.uuid = df.fileresource_uuid";

  // select only what is shown/needed in paged list (to avoid unnecessary payload/traffic):
  private static final String REDUCED_FIND_ONE_BASE_SQL =
      "SELECT d.uuid d_uuid, d.refid d_refId, d.label d_label, d.description d_description,"
          + " d.identifiable_type d_type, d.entity_type d_entityType,"
          + " d.created d_created, d.last_modified d_lastModified,"
          + " file.uuid f_uuid, file.uri f_uri, file.filename f_filename"
          + " FROM digitalobjects as d"
          + " LEFT JOIN fileresources_image as file on d.previewfileresource = file.uuid";

  private final FileResourceMetadataRepository fileResourceMetadataRepository;

  @Autowired
  public DigitalObjectRepositoryImpl(
      Jdbi dbi,
      IdentifierRepository identifierRepository,
      FileResourceMetadataRepository fileResourceMetadataRepository) {
    super(dbi, identifierRepository);
    this.fileResourceMetadataRepository = fileResourceMetadataRepository;
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM digitalobjects";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOne().get());
    return count;
  }

  @Override
  public PageResponse<DigitalObject> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder(REDUCED_FIND_ONE_BASE_SQL);
    addPageRequestParams(pageRequest, query);

    List<DigitalObjectImpl> result =
        dbi.withHandle(
            h ->
                h.createQuery(query.toString())
                    .registerRowMapper(BeanMapper.factory(DigitalObjectImpl.class, "d"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                    .reduceRows(
                        new LinkedHashMap<UUID, IdentifiableAggregator<DigitalObjectImpl>>(),
                        (map, rowView) -> {
                          IdentifiableAggregator<DigitalObjectImpl> aggregator =
                              map.computeIfAbsent(
                                  rowView.getColumn("d_uuid", UUID.class),
                                  fn -> {
                                    return new IdentifiableAggregator<>(
                                        rowView.getRow(DigitalObjectImpl.class));
                                  });
                          DigitalObjectImpl obj = aggregator.identifiable;
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
  public DigitalObject findByIdentifier(String namespace, String id) {
    return findOne(new IdentifierImpl(null, namespace, id));
  }

  @Override
  public DigitalObject findOne(UUID uuid) {
    String query = FIND_ONE_BASE_SQL + " WHERE d.uuid = :uuid";

    DigitalObjectImpl result =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bind("uuid", uuid)
                    .registerRowMapper(BeanMapper.factory(DigitalObjectImpl.class, "d"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                    .registerRowMapper(BeanMapper.factory(FileResourceImpl.class, "fr"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    .reduceRows(
                        new DigitalObjectRowReducer(),
                        (rowReducer, rowView) -> {
                          if (rowReducer.identifiable == null) {
                            rowReducer.identifiable = rowView.getRow(DigitalObjectImpl.class);
                          }
                          DigitalObjectImpl obj = rowReducer.identifiable;

                          if (rowView.getColumn("f_uuid", UUID.class) != null) {
                            obj.setPreviewImage(rowView.getRow(ImageFileResourceImpl.class));
                          }

                          final UUID idUuid = rowView.getColumn("id_uuid", UUID.class);
                          if (idUuid != null && !rowReducer.identifiers.contains(idUuid)) {
                            IdentifierImpl identifier = rowView.getRow(IdentifierImpl.class);
                            obj.addIdentifier(identifier);
                            rowReducer.identifiers.add(idUuid);
                          }

                          final UUID frUuid = rowView.getColumn("fr_uuid", UUID.class);
                          if (frUuid != null && !rowReducer.fileResources.contains(frUuid)) {
                            FileResourceImpl fileResource = rowView.getRow(FileResourceImpl.class);
                            obj.addFileResource(fileResource);
                            rowReducer.fileResources.add(frUuid);
                          }
                          return rowReducer;
                        })
                    .identifiable);
    return result;
  }

  @Override
  public DigitalObject findOne(Identifier identifier) {
    if (identifier.getIdentifiable() != null) {
      return findOne(identifier.getIdentifiable());
    }

    String namespace = identifier.getNamespace();
    String identifierId = identifier.getId();

    String query = FIND_ONE_BASE_SQL + " WHERE id.identifier = :id AND id.namespace = :namespace";

    DigitalObjectImpl result =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bind("id", identifierId)
                    .bind("namespace", namespace)
                    .registerRowMapper(BeanMapper.factory(DigitalObjectImpl.class, "d"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                    .registerRowMapper(BeanMapper.factory(FileResourceImpl.class, "fr"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    .reduceRows(
                        new DigitalObjectRowReducer(),
                        (rowReducer, rowView) -> {
                          if (rowReducer.identifiable == null) {
                            rowReducer.identifiable = rowView.getRow(DigitalObjectImpl.class);
                          }
                          DigitalObjectImpl obj = rowReducer.identifiable;

                          if (rowView.getColumn("f_uuid", UUID.class) != null) {
                            obj.setPreviewImage(rowView.getRow(ImageFileResourceImpl.class));
                          }

                          final UUID idUuid = rowView.getColumn("id_uuid", UUID.class);
                          if (idUuid != null && !rowReducer.identifiers.contains(idUuid)) {
                            IdentifierImpl newIdentifier = rowView.getRow(IdentifierImpl.class);
                            obj.addIdentifier(newIdentifier);
                            rowReducer.identifiers.add(idUuid);
                          }

                          final UUID frUuid = rowView.getColumn("fr_uuid", UUID.class);
                          if (frUuid != null && !rowReducer.fileResources.contains(frUuid)) {
                            FileResourceImpl fileResource = rowView.getRow(FileResourceImpl.class);
                            obj.addFileResource(fileResource);
                            rowReducer.fileResources.add(frUuid);
                          }
                          return rowReducer;
                        })
                    .identifiable);
    return result;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[] {"d.last_modified"};
  }

  @Override
  public LinkedHashSet<FileResource> getFileResources(UUID digitalObjectUuid) {
    String query =
        "SELECT f.uuid f_uuid, f.label f_label,"
            + " f.created f_created, f.last_modified f_lastModified,"
            + " f.filename f_filename, f.mimetype f_mimeType, f.size_in_bytes f_sizeInBytes, f.uri f_uri,"
            + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
            + " file.uuid pf_uuid, file.filename pf_filename, file.mimetype pf_mimetype, file.size_in_bytes pf_size_in_bytes, file.uri pf_uri"
            + " FROM fileresources as f"
            + " LEFT JOIN identifiers as id on f.uuid = id.identifiable"
            + " LEFT JOIN fileresources_image as file on f.previewfileresource = file.uuid"
            + " LEFT JOIN digitalobject_fileresources as df on f.uuid = df.fileresource_uuid"
            + " WHERE df.digitalobject_uuid = :uuid"
            + " ORDER BY df.sortIndex ASC";

    List<FileResourceImpl> result =
        dbi.withHandle(
            h ->
                h.createQuery(query).bind("uuid", digitalObjectUuid)
                    .registerRowMapper(BeanMapper.factory(FileResourceImpl.class, "f"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pf"))
                    .reduceRows(
                        new LinkedHashMap<UUID, IdentifiableAggregator<FileResourceImpl>>(),
                        (map, rowView) -> {
                          IdentifiableAggregator<FileResourceImpl> aggregator =
                              map.computeIfAbsent(
                                  rowView.getColumn("f_uuid", UUID.class),
                                  fn -> {
                                    return new IdentifiableAggregator<>(
                                        rowView.getRow(FileResourceImpl.class));
                                  });
                          FileResourceImpl obj = aggregator.identifiable;

                          if (rowView.getColumn("pf_uuid", UUID.class) != null) {
                            obj.setPreviewImage(rowView.getRow(ImageFileResourceImpl.class));
                          }

                          final UUID idUuid = rowView.getColumn("id_uuid", UUID.class);
                          if (idUuid != null && !aggregator.identifiers.contains(idUuid)) {
                            IdentifierImpl identifier = rowView.getRow(IdentifierImpl.class);
                            obj.addIdentifier(identifier);
                            aggregator.identifiers.add(idUuid);
                          }

                          return map;
                        })
                    .values().stream()
                    .map(aggregator -> aggregator.identifiable)
                    .collect(Collectors.toList()));
    return new LinkedHashSet<>(result);
  }

  @Override
  public LinkedHashSet<ImageFileResource> getImageFileResources(UUID digitalObjectUuid) {
    String query =
        "SELECT f.uuid f_uuid, f.label f_label,"
            + " f.created f_created, f.last_modified f_lastModified,"
            + " f.filename f_filename, f.mimetype f_mimeType, f.size_in_bytes f_sizeInBytes, f.uri f_uri,"
            + " f.height f_height, f.width f_width,"
            + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
            + " file.uuid pf_uuid, file.filename pf_filename, file.mimetype pf_mimetype, file.size_in_bytes pf_size_in_bytes, file.uri pf_uri"
            + " FROM fileresources_image as f"
            + " LEFT JOIN identifiers as id on f.uuid = id.identifiable"
            + " LEFT JOIN fileresources_image as file on f.previewfileresource = file.uuid"
            + " LEFT JOIN digitalobject_fileresources as df on f.uuid = df.fileresource_uuid"
            + " WHERE df.digitalobject_uuid = :uuid"
            + " ORDER BY df.sortIndex ASC";

    List<ImageFileResourceImpl> result =
        dbi.withHandle(
            h ->
                h.createQuery(query).bind("uuid", digitalObjectUuid)
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    // TODO: test it if it is working, because I think there were problems using two
                    // rowmappers of same type...
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pf"))
                    .reduceRows(
                        new LinkedHashMap<UUID, IdentifiableAggregator<ImageFileResourceImpl>>(),
                        (map, rowView) -> {
                          IdentifiableAggregator<ImageFileResourceImpl> aggregator =
                              map.computeIfAbsent(
                                  rowView.getColumn("f_uuid", UUID.class),
                                  fn -> {
                                    return new IdentifiableAggregator<>(
                                        rowView.getRow(ImageFileResourceImpl.class));
                                  });
                          ImageFileResourceImpl obj = aggregator.identifiable;

                          if (rowView.getColumn("pf_uuid", UUID.class) != null) {
                            obj.setPreviewImage(rowView.getRow(ImageFileResourceImpl.class));
                          }

                          final UUID idUuid = rowView.getColumn("id_uuid", UUID.class);
                          if (idUuid != null && !aggregator.identifiers.contains(idUuid)) {
                            IdentifierImpl identifier = rowView.getRow(IdentifierImpl.class);
                            obj.addIdentifier(identifier);
                            aggregator.identifiers.add(idUuid);
                          }

                          return map;
                        })
                    .values().stream()
                    .map(aggregator -> aggregator.identifiable)
                    .collect(Collectors.toList()));
    return new LinkedHashSet<>(result);
  }

  private int getIndex(LinkedHashSet<FileResource> fileResources, FileResource fileResource) {
    int pos = -1;
    for (Iterator<FileResource> iterator = fileResources.iterator(); iterator.hasNext(); ) {
      pos = pos + 1;
      FileResource fr = iterator.next();
      if (fr.getUuid().equals(fileResource.getUuid())) {
        return pos;
      }
    }
    return -1;
  }

  @Override
  public DigitalObject save(DigitalObject digitalObject) {
    digitalObject.setUuid(UUID.randomUUID());
    digitalObject.setCreated(LocalDateTime.now());
    digitalObject.setLastModified(LocalDateTime.now());
    // refid is generated as serial, DO NOT SET!
    final UUID previewImageUuid =
        digitalObject.getPreviewImage() == null ? null : digitalObject.getPreviewImage().getUuid();

    String query =
        "INSERT INTO digitalobjects("
            + "uuid, label, description, previewFileResource,"
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
                .bindBean(digitalObject)
                .execute());

    // for now we implement first interesting use case: new digital object with new fileresources...
    final LinkedHashSet<FileResource> fileResources = digitalObject.getFileResources();
    saveFileResources(digitalObject, fileResources);

    // save identifiers
    List<Identifier> identifiers = digitalObject.getIdentifiers();
    saveIdentifiers(identifiers, digitalObject);

    DigitalObject result = findOne(digitalObject.getUuid());
    return result;
  }

  @Override
  public LinkedHashSet<FileResource> saveFileResources(
      DigitalObject digitalObject, LinkedHashSet<FileResource> fileResources) {
    if (fileResources == null) {
      return null;
    }
    return saveFileResources(digitalObject.getUuid(), fileResources);
  }

  @Override
  public LinkedHashSet<FileResource> saveFileResources(
      UUID digitalObjectUuid, LinkedHashSet<FileResource> fileResources) {

    // as we store the whole list new: delete old entries
    dbi.withHandle(
        h ->
            h.createUpdate(
                    "DELETE FROM digitalobject_fileresources WHERE digitalobject_uuid = :uuid")
                .bind("uuid", digitalObjectUuid)
                .execute());

    if (fileResources != null) {
      // first save fileresources
      for (FileResource fileResource : fileResources) {
        if (fileResource.getUuid() == null) {
          fileResource = fileResourceMetadataRepository.save(fileResource);
        }
      }

      // second: save relations to digital object
      dbi.useHandle(
          handle -> {
            PreparedBatch preparedBatch =
                handle.prepareBatch(
                    "INSERT INTO digitalobject_fileresources(digitalobject_uuid, fileresource_uuid, sortIndex) VALUES(:uuid, :fileResourceUuid, :sortIndex)");
            for (FileResource fileResource : fileResources) {
              preparedBatch
                  .bind("uuid", digitalObjectUuid)
                  .bind("fileResourceUuid", fileResource.getUuid())
                  .bind("sortIndex", getIndex(fileResources, fileResource))
                  .add();
            }
            preparedBatch.execute();
          });
    }
    return getFileResources(digitalObjectUuid);
  }

  @Override
  public DigitalObject update(DigitalObject digitalObject) {
    digitalObject.setLastModified(LocalDateTime.now());

    // do not update/left out from statement (not changed since insert):
    // uuid, created, identifiable_type, entity_type, refid
    final UUID previewImageUuid =
        digitalObject.getPreviewImage() == null ? null : digitalObject.getPreviewImage().getUuid();

    String query =
        "UPDATE digitalobjects SET"
            + " label=:label::JSONB, description=:description::JSONB, previewFileResource=:previewFileResource,"
            + " last_modified=:lastModified"
            + " WHERE uuid=:uuid";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(digitalObject)
                .execute());

    // save identifiers
    // as we store the whole list new: delete old entries
    deleteIdentifiers(digitalObject);
    List<Identifier> identifiers = digitalObject.getIdentifiers();
    saveIdentifiers(identifiers, digitalObject);

    DigitalObject result = findOne(digitalObject.getUuid());
    return result;
  }

  static class DigitalObjectRowReducer extends IdentifiableAggregator<DigitalObjectImpl> {

    Set<UUID> fileResources = new HashSet<>();
  }
}
