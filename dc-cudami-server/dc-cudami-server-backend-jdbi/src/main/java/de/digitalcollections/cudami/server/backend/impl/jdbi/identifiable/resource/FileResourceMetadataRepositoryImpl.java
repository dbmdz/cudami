package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.FileResourceMetadataRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.resource.ApplicationFileResource;
import de.digitalcollections.model.api.identifiable.resource.AudioFileResource;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.api.identifiable.resource.MimeType;
import de.digitalcollections.model.api.identifiable.resource.TextFileResource;
import de.digitalcollections.model.api.identifiable.resource.VideoFileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.IdentifierImpl;
import de.digitalcollections.model.impl.identifiable.resource.ApplicationFileResourceImpl;
import de.digitalcollections.model.impl.identifiable.resource.AudioFileResourceImpl;
import de.digitalcollections.model.impl.identifiable.resource.FileResourceImpl;
import de.digitalcollections.model.impl.identifiable.resource.ImageFileResourceImpl;
import de.digitalcollections.model.impl.identifiable.resource.TextFileResourceImpl;
import de.digitalcollections.model.impl.identifiable.resource.VideoFileResourceImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class FileResourceMetadataRepositoryImpl extends IdentifiableRepositoryImpl<FileResource> implements FileResourceMetadataRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileResourceMetadataRepositoryImpl.class);

  static final String SELECT_ALL = "select f.uuid f_uuid, created f_created, description f_description, identifiable_type f_identifiable_type,"
                                   + " label f_label, last_modified f_last_modified, filename f_filename, mimetype f_mimetype, size_in_bytes f_size_in_bytes, uri f_uri,"
                                   + " id.uuid id_uuid, identifiable id_identifiable, namespace id_namspace, identifier id_identifier"
                                   + " from fileresources as f left join identifiers as id on f.uuid = id.identifiable";

  private final IdentifierRepository identifierRepository;

  @Autowired
  public FileResourceMetadataRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository) {
    super(dbi);
    this.identifierRepository = identifierRepository;
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM fileresources";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOne().get());
    return count;
  }

  @Override
  public FileResource createByMimeType(MimeType mimeType) {
    if (mimeType == null) {
      mimeType = MimeType.MIME_APPLICATION_OCTET_STREAM;
    }
    FileResource result;
    String primaryType = mimeType.getPrimaryType();
    switch (primaryType) {
      case "audio":
        result = new AudioFileResourceImpl();
        break;
      case "image":
        result = new ImageFileResourceImpl();
        break;
      case "text":
        result = new TextFileResourceImpl();
        break;
      case "video":
        result = new VideoFileResourceImpl();
        break;
      default:
        result = new ApplicationFileResourceImpl();
    }
    result.setMimeType(mimeType);
    final UUID uuid = UUID.randomUUID();
    result.setUuid(uuid);
    return result;
  }

  @Override
  public PageResponse<FileResource> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder("SELECT " + IDENTIFIABLE_COLUMNS + ", filename, mimetype, size_in_bytes, uri")
      .append(" FROM fileresources");

    addPageRequestParams(pageRequest, query);
    List<? extends FileResource> result = dbi.withHandle(h -> h.createQuery(query.toString())
      //        .mapToBean(FileResourceImpl.class)
      .map(new FileResourceMapper())
      .list());
    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public FileResource findOne(UUID uuid) {
    // TODO maybe just get mimetype value and make fileresource specific queries....
    /*
    StringBuilder query = new StringBuilder("SELECT " + IDENTIFIABLE_COLUMNS + ", filename, mimetype, size_in_bytes, uri")
        .append(" FROM fileresources")
        .append(" WHERE uuid = :uuid");
     */
    StringBuilder query = new StringBuilder(SELECT_ALL).append(" WHERE f.uuid = :uuid");
    Optional<FileResource> fileResourceOpt = dbi.withHandle(h -> h.createQuery(query.toString())
      .bind("uuid", uuid)
      .registerRowMapper(BeanMapper.factory(FileResourceImpl.class, "f"))
      .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
      .reduceRows(
        new LinkedHashMap<UUID, FileResource>(), (map, rowView) -> {
        FileResource fr = map.computeIfAbsent(rowView.getColumn("f_uuid", UUID.class), id -> rowView.getRow(FileResourceImpl.class));
        if (rowView.getColumn("id_uuid", UUID.class) != null) {
          fr.addIdentifier(rowView.getRow(IdentifierImpl.class));
        }
        return map;
      })
      .values()
      .stream()
      .findFirst());
    if (!fileResourceOpt.isPresent()) {
      return null;
    }
    FileResource fileResource = fileResourceOpt.get();

    addSpecificMetadata(fileResource, uuid);

    return fileResource;
  }

  private void addSpecificMetadata(FileResource fileResource, UUID uuid) {
    if (fileResource instanceof ApplicationFileResource) {
      // no special fields, yet
    } else if (fileResource instanceof AudioFileResource) {
      int result = dbi.withHandle(h -> h.createQuery("SELECT duration FROM fileresources_audio WHERE uuid = :uuid")
        .bind("uuid", uuid)
        .mapTo(Integer.class)
        .findOne().orElse(null));
      ((AudioFileResource) fileResource).setDuration(result);
    } else if (fileResource instanceof ImageFileResource) {
      Map<String, Object> result = dbi.withHandle(h -> h.createQuery("SELECT width, height FROM fileresources_image WHERE uuid = :uuid")
        .bind("uuid", uuid)
        .mapToMap()
        .findOne().orElse(null));
      ((ImageFileResource) fileResource).setWidth((int) result.get("width"));
      ((ImageFileResource) fileResource).setHeight((int) result.get("height"));
    } else if (fileResource instanceof TextFileResource) {
      // no special fields, yet
    } else if (fileResource instanceof VideoFileResource) {
      int result = dbi.withHandle(h -> h.createQuery("SELECT duration FROM fileresources_video WHERE uuid = :uuid")
        .bind("uuid", uuid)
        .mapTo(Integer.class)
        .findOne().orElse(null));
      ((VideoFileResource) fileResource).setDuration(result);
    }
  }

  @Override
  public FileResource findOne(Identifier identifier) {
    if (identifier.getUuid() != null) {
      return findOne(identifier.getUuid());
    }

    String namespace = identifier.getNamespace();
    String id = identifier.getId();

    String query = "select f.*"
                   + " from fileresources as f"
                   + " left join identifiers as id on f.uuid = id.identifiable"
                   + " left join digitalobject_fileresources as df on df.fileresource_uuid = f.uuid"
                   + " left join digitalobjects as di on di.uuid = df.digitalobject_uuid"
                   + " left join versions as v on di.version = v.uuid"
                   + " where id.identifier = :id"
                   + " and id.namespace = :namespace"
                   + " and v.status = 'active'";

    FileResource fileResource = dbi.withHandle(h -> h.createQuery(query)
      .bind("id", id)
      .bind("namespace", namespace)
      .map(new FileResourceMapper())
      .findOne().orElse(null));

    addSpecificMetadata(fileResource, fileResource.getUuid());

    return fileResource;
  }

  @Override
  public FileResource save(FileResource fileResource) {
    if (fileResource.getUuid() == null) {
      fileResource.setUuid(UUID.randomUUID());
    }
    fileResource.setCreated(LocalDateTime.now());
    fileResource.setLastModified(LocalDateTime.now());

    final String baseColumnsSql = "uuid, created, description, identifiable_type, label, last_modified, filename, mimetype, size_in_bytes, uri";
    final String basePropertiesSql = ":uuid, :created, :description::JSONB, :type, :label::JSONB, :lastModified, :filename, :mimeType, :sizeInBytes, :uri";

    if (fileResource instanceof ApplicationFileResource) {
      // no special columns
      dbi.withHandle(h -> h.createUpdate("INSERT INTO fileresources_application(" + baseColumnsSql + ") VALUES (" + basePropertiesSql + ")")
        .bindBean(fileResource)
        .execute());
    } else if (fileResource instanceof AudioFileResource) {
      dbi.withHandle(h -> h.createUpdate("INSERT INTO fileresources_audio(" + baseColumnsSql + ", duration) VALUES (" + basePropertiesSql + ", :duration)")
        .bindBean(fileResource)
        .execute());
    } else if (fileResource instanceof ImageFileResource) {
      dbi.withHandle(h -> h.createUpdate("INSERT INTO fileresources_image(" + baseColumnsSql + ", width, height) VALUES (" + basePropertiesSql + ", :width, :height)")
        .bindBean(fileResource)
        .execute());
    } else if (fileResource instanceof TextFileResource) {
      // no special columns
      dbi.withHandle(h -> h.createUpdate("INSERT INTO fileresources_text(" + baseColumnsSql + ") VALUES (" + basePropertiesSql + ")")
        .bindBean(fileResource)
        .execute());
    } else if (fileResource instanceof VideoFileResource) {
      dbi.withHandle(h -> h.createUpdate("INSERT INTO fileresources_video(" + baseColumnsSql + ", duration) VALUES (" + basePropertiesSql + ", :duration)")
        .bindBean(fileResource)
        .execute());
    } else {
      throw new IllegalArgumentException("unknown file resource type " + fileResource.getMimeType().toString());
    }

    // save file resource identifiers
    List<Identifier> identifiers = fileResource.getIdentifiers();
    if (identifiers != null) {
      for (Identifier identifier : identifiers) {
        identifier.setIdentifiable(fileResource.getUuid());
        // newly created file resource, no pre existing identifiers, so just save
        identifierRepository.save(identifier);
      }
    }

    FileResource dbFileResource = findOne(fileResource.getUuid());
    return dbFileResource;
  }

  @Override
  public FileResource update(FileResource fileResource) {
    fileResource.setLastModified(LocalDateTime.now());

    final String baseColumnsSql = "description=:description::JSONB, label=:label::JSONB, last_modified=:lastModified, filename=:filename, mimetype=:mimeType, size_in_bytes=:sizeInBytes, uri=:uri";

    FileResource result;
    if (fileResource instanceof ApplicationFileResource) {
      // no special columns
      result = dbi.withHandle(h -> h.createQuery("UPDATE fileresources_application SET " + baseColumnsSql + " WHERE uuid=:uuid RETURNING *")
        .bindBean(fileResource)
        .mapToBean(ApplicationFileResourceImpl.class)
        .findOne().orElse(null));
    } else if (fileResource instanceof AudioFileResource) {
      result = dbi.withHandle(h -> h.createQuery("UPDATE fileresources_audio SET " + baseColumnsSql + ", duration=:duration WHERE uuid=:uuid RETURNING *")
        .bindBean(fileResource)
        .mapToBean(AudioFileResourceImpl.class)
        .findOne().orElse(null));
    } else if (fileResource instanceof ImageFileResource) {
      result = dbi.withHandle(h -> h.createQuery("UPDATE fileresources_image SET " + baseColumnsSql + ", width=:width, height=:height WHERE uuid=:uuid RETURNING *")
        .bindBean(fileResource)
        .mapToBean(ImageFileResourceImpl.class)
        .findOne().orElse(null));
    } else if (fileResource instanceof TextFileResource) {
      // no special columns
      result = dbi.withHandle(h -> h.createQuery("UPDATE fileresources_text SET " + baseColumnsSql + " WHERE uuid=:uuid RETURNING *")
        .bindBean(fileResource)
        .mapToBean(TextFileResourceImpl.class)
        .findOne().orElse(null));
    } else if (fileResource instanceof VideoFileResource) {
      result = dbi.withHandle(h -> h.createQuery("UPDATE fileresources_video SET " + baseColumnsSql + ", duration=:duration WHERE uuid=:uuid RETURNING *")
        .bindBean(fileResource)
        .mapToBean(VideoFileResourceImpl.class)
        .findOne().orElse(null));
    } else {
      throw new IllegalArgumentException("unknown file resource type " + fileResource.getMimeType().toString());
    }
    return result;
  }
}
