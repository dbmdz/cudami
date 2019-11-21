package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import static java.util.stream.Collectors.toList;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.DigitalObjectRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.FileResourceMetadataRepository;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.IdentifierImpl;
import de.digitalcollections.model.impl.identifiable.entity.DigitalObjectImpl;
import de.digitalcollections.model.impl.identifiable.resource.ApplicationFileResourceImpl;
import de.digitalcollections.model.impl.identifiable.resource.AudioFileResourceImpl;
import de.digitalcollections.model.impl.identifiable.resource.FileResourceImpl;
import de.digitalcollections.model.impl.identifiable.resource.ImageFileResourceImpl;
import de.digitalcollections.model.impl.identifiable.resource.TextFileResourceImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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

  private final FileResourceMetadataRepository fileResourceMetadataRepository;
  private final IdentifierRepository identifierRepository;

  @Autowired
  public DigitalObjectRepositoryImpl(
      Jdbi dbi,
      IdentifierRepository identifierRepository,
      FileResourceMetadataRepository fileResourceMetadataRepository) {
    super(dbi);
    this.fileResourceMetadataRepository = fileResourceMetadataRepository;
    this.identifierRepository = identifierRepository;
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM digitalobjects";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOne().get());
    return count;
  }

  @Override
  public PageResponse<DigitalObject> find(PageRequest pageRequest) {
    StringBuilder query =
        new StringBuilder(
            "SELECT uuid, label, description,"
                + " created, last_modified"
                + " FROM digitalobjects");
    addPageRequestParams(pageRequest, query);

    List<DigitalObjectImpl> result =
        dbi.withHandle(
            h -> h.createQuery(query.toString()).mapToBean(DigitalObjectImpl.class).list());
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
    String query =
        "SELECT d.uuid d_uuid, d.label d_label, d.description d_description,"
            + " d.identifiable_type d_identifiable_type, d.entity_type d_entity_type,"
            + " d.created d_created, d.last_modified d_last_modified,"
            + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
            + " df.fileresource_uuid df_fileresource_uuid,"
            + ""
            + " fi.uuid fi_uuid, fi.label fi_label, fi.description fi_description,"
            + " fi.identifiable_type fi_identifiable_type,"
            + " fi.created fi_created, fi.last_modified fi_last_modified,"
            + " fi.filename fi_filename, fi.mimetype fi_mimetype, fi.size_in_bytes fi_size_in_bytes, fi.uri fi_uri,"
            + " fi.height fi_height, fi.width fi_width,"
            + ""
            + " fa.uuid fa_uuid, fa.label fa_label, fa.description fa_description,"
            + " fa.identifiable_type fa_identifiable_type,"
            + " fa.created fa_created, fa.last_modified fa_last_modified,"
            + " fa.filename fa_filename, fa.mimetype fa_mimetype, fa.size_in_bytes fa_size_in_bytes, fa.uri fa_uri,"
            + " fa.duration fa_duration,"
            + ""
            + " ft.uuid ft_uuid, ft.label ft_label, ft.description ft_description,"
            + " ft.identifiable_type ft_identifiable_type,"
            + " ft.created ft_created, ft.last_modified ft_last_modified,"
            + " ft.filename ft_filename, ft.mimetype ft_mimetype, ft.size_in_bytes ft_size_in_bytes, ft.uri ft_uri,"
            + ""
            + " fp.uuid fp_uuid, fp.label fp_label, fp.description fp_description,"
            + " fp.identifiable_type fp_identifiable_type,"
            + " fp.created fp_created, fp.last_modified fp_last_modified,"
            + " fp.filename fp_filename, fp.mimetype fp_mimetype, fp.size_in_bytes fp_size_in_bytes, fp.uri fp_uri"
            + ""
            + " FROM digitalobjects as d"
            + " LEFT JOIN identifiers as id on d.uuid = id.identifiable"
            + " LEFT JOIN digitalobject_fileresources as df on d.uuid = df.digitalobject_uuid"
            + " LEFT JOIN fileresources_image as fi on df.fileresource_uuid = fi.uuid"
            + " LEFT JOIN fileresources_audio as fa on df.fileresource_uuid = fa.uuid"
            + " LEFT JOIN fileresources_text as ft on df.fileresource_uuid = ft.uuid"
            + " LEFT JOIN fileresources_application as fp on df.fileresource_uuid = fp.uuid"
            + " WHERE d.uuid = :uuid"
            + " ORDER BY df.sortindex";

    Optional<DigitalObjectImpl> digitalObjectOpt =
        dbi.withHandle(
            h ->
                h.createQuery(query).bind("uuid", uuid)
                    .registerRowMapper(BeanMapper.factory(DigitalObjectImpl.class, "d"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "fi"))
                    .registerRowMapper(BeanMapper.factory(AudioFileResourceImpl.class, "fa"))
                    .registerRowMapper(BeanMapper.factory(TextFileResourceImpl.class, "ft"))
                    .registerRowMapper(BeanMapper.factory(ApplicationFileResourceImpl.class, "fp"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    .reduceRows(
                        new DigitalObjectAggregatorImpl(),
                        (map, rowView) -> {
                          UUID dUUID = rowView.getColumn("d_uuid", UUID.class);
                          DigitalObjectImpl digitalObject =
                              map.getDigitalObjects()
                                  .computeIfAbsent(
                                      dUUID, id -> rowView.getRow(DigitalObjectImpl.class));

                          UUID idUUID = rowView.getColumn("id_uuid", UUID.class);
                          if (idUUID != null && !map.getIdentifiers().containsKey(idUUID)) {
                            Identifier identifier = rowView.getRow(IdentifierImpl.class);
                            digitalObject.getIdentifiers().add(identifier);
                            map.getIdentifiers().put(idUUID, identifier);
                          }

                          UUID fiUUID = rowView.getColumn("fi_uuid", UUID.class);
                          UUID dfFileresourceUuid =
                              rowView.getColumn("df_fileresource_uuid", UUID.class);
                          if (!dfFileresourceUuid.equals(map.getDfFileresourceUuid())) {
                            if (fiUUID != null) {
                              ImageFileResource imageFileResource =
                                  rowView.getRow(ImageFileResourceImpl.class);
                              digitalObject.addFileResource(imageFileResource);
                              map.setDfFileresourceUuid(dfFileresourceUuid);
                            } else if (rowView.getColumn("fa_uuid", UUID.class) != null) {
                              FileResource fileResource =
                                  rowView.getRow(AudioFileResourceImpl.class);
                              digitalObject.addFileResource(fileResource);
                              map.setDfFileresourceUuid(dfFileresourceUuid);
                            } else if (rowView.getColumn("ft_uuid", UUID.class) != null) {
                              FileResource fileResource =
                                  rowView.getRow(TextFileResourceImpl.class);
                              digitalObject.addFileResource(fileResource);
                              map.setDfFileresourceUuid(dfFileresourceUuid);
                            } else if (rowView.getColumn("fp_uuid", UUID.class) != null) {
                              FileResource fileResource =
                                  rowView.getRow(ApplicationFileResourceImpl.class);
                              digitalObject.addFileResource(fileResource);
                              map.setDfFileresourceUuid(dfFileresourceUuid);
                            }
                          }
                          return map;
                        })
                    .getDigitalObjects().values().stream()
                    .findFirst());

    if (digitalObjectOpt.isPresent()) {
      return digitalObjectOpt.get();
    } else {
      return null;
    }
  }

  @Override
  public DigitalObject findOne(Identifier identifier) {
    if (identifier.getIdentifiable() != null) {
      return findOne(identifier.getIdentifiable());
    }

    String namespace = identifier.getNamespace();
    String id = identifier.getId();

    String query =
        "SELECT d.uuid"
            + " FROM digitalobjects as d"
            + " LEFT JOIN identifiers as id on d.uuid = id.identifiable"
            + " LEFT JOIN versions as v on v.uuid = d.version"
            + " WHERE id.identifier = :id AND id.namespace = :namespace AND v.status = 'ACTIVE'";

    UUID uuid =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bind("id", id)
                    .bind("namespace", namespace)
                    .mapTo(UUID.class)
                    .findOne()
                    .orElse(null));
    if (uuid == null) {
      return null;
    }
    return findOne(uuid);
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[] {"last_modified"};
  }

  @Override
  public LinkedHashSet<FileResource> getFileResources(UUID digitalObjectUuid) {
    String query =
        "SELECT f.*"
            + " FROM fileresources as f"
            + " LEFT JOIN digitalobject_fileresources as df on f.uuid = df.fileresource_uuid"
            + " WHERE df.digitalobject_uuid = :uuid"
            + " ORDER BY df.sortIndex ASC";

    List<FileResourceImpl> result =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bind("uuid", digitalObjectUuid)
                    .mapToBean(FileResourceImpl.class)
                    .list());
    return new LinkedHashSet<>(result);
  }

  @Override
  public LinkedHashSet<ImageFileResource> getImageFileResources(UUID digitalObjectUuid) {
    String query =
        "SELECT"
            + " fi.uuid fi_uuid, fi.label fi_label, fi.description fi_description,"
            + " fi.identifiable_type fi_identifiable_type,"
            + " fi.created fi_created, fi.last_modified fi_last_modified,"
            + " fi.filename fi_filename, fi.mimetype fi_mimetype, fi.size_in_bytes fi_size_in_bytes, fi.uri fi_uri,"
            + " fi.height fi_height, fi.width fi_width,"
            + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
            + " FROM fileresources_image as fi"
            + " LEFT JOIN digitalobject_fileresources as df on fi.uuid = df.fileresource_uuid"
            + " LEFT JOIN identifiers as id on fi.uuid = id.identifiable"
            + " WHERE df.digitalobject_uuid = :uuid"
            + " ORDER BY df.sortIndex ASC";

    List<ImageFileResourceImpl> result =
        dbi.withHandle(
            h ->
                h.createQuery(query).bind("uuid", digitalObjectUuid)
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "fi"))
                    .reduceRows(
                        new LinkedHashMap<UUID, ImageFileResourceImpl>(),
                        (map, rowView) -> {
                          ImageFileResource imageFileResource =
                              map.computeIfAbsent(
                                  rowView.getColumn("fi_uuid", UUID.class),
                                  id -> rowView.getRow(ImageFileResourceImpl.class));
                          if (rowView.getColumn("id_uuid", UUID.class) != null) {
                            imageFileResource.addIdentifier(rowView.getRow(IdentifierImpl.class));
                          }
                          return map;
                        })
                    .values().stream()
                    .collect(toList()));

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

    dbi.withHandle(
        h ->
            h.createUpdate(
                    "INSERT INTO digitalobjects("
                        + "uuid, label, description, identifiable_type, entity_type, created, last_modified, version"
                        + ") VALUES ("
                        + ":uuid, :label::JSONB, :description::JSONB, :type, :entityType, :created, :lastModified, :version.uuid"
                        + ")")
                .bindBean(digitalObject)
                .execute());

    // for now we implement first interesting use case: new digital object with new fileresources...
    final LinkedHashSet<FileResource> fileResources = digitalObject.getFileResources();
    saveFileResources(digitalObject, fileResources);

    // save digital object identifiers
    List<Identifier> identifiers = digitalObject.getIdentifiers();
    for (Identifier identifier : identifiers) {
      identifier.setIdentifiable(digitalObject.getUuid());
      // newly created digital object, no pre existing identifiers, so just save
      identifierRepository.save(identifier);
    }

    DigitalObject dbDigitalObject = findOne(digitalObject.getUuid());
    return dbDigitalObject;
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
        fileResource = fileResourceMetadataRepository.save(fileResource);
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

    // do not update/left out from statement (not changed since insert): uuid, created,
    // identifiable_type, entity_type
    DigitalObject result =
        dbi.withHandle(
            h ->
                h.createQuery(
                        "UPDATE digitalobjects SET description=:description::JSONB, label=:label::JSONB, last_modified=:lastModified WHERE uuid=:uuid RETURNING *")
                    .bindBean(digitalObject)
                    .mapToBean(DigitalObjectImpl.class)
                    .findOne()
                    .orElse(null));
    return result;
  }
}
