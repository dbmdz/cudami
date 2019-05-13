package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource;

import de.digitalcollections.commons.file.backend.impl.managed.ManagedFileResourceRepositoryImpl;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.CudamiFileResourceRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.model.api.identifiable.resource.ApplicationFileResource;
import de.digitalcollections.model.api.identifiable.resource.AudioFileResource;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.api.identifiable.resource.TextFileResource;
import de.digitalcollections.model.api.identifiable.resource.VideoFileResource;
import de.digitalcollections.model.api.identifiable.resource.exceptions.ResourceIOException;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.resource.ApplicationFileResourceImpl;
import de.digitalcollections.model.impl.identifiable.resource.AudioFileResourceImpl;
import de.digitalcollections.model.impl.identifiable.resource.ImageFileResourceImpl;
import de.digitalcollections.model.impl.identifiable.resource.TextFileResourceImpl;
import de.digitalcollections.model.impl.identifiable.resource.VideoFileResourceImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CudamiFileResourceRepositoryImpl extends IdentifiableRepositoryImpl<FileResource> implements CudamiFileResourceRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(CudamiFileResourceRepositoryImpl.class);

  ManagedFileResourceRepositoryImpl fileResourceRepository;

  @Autowired
  public CudamiFileResourceRepositoryImpl(Jdbi dbi, ManagedFileResourceRepositoryImpl fileResourceRepository) {
    super(dbi);
    this.fileResourceRepository = fileResourceRepository;
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM fileresources";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOnly());
    return count;
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

    StringBuilder query = new StringBuilder("SELECT " + IDENTIFIABLE_COLUMNS + ", filename, mimetype, size_in_bytes, uri")
        .append(" FROM fileresources")
        .append(" WHERE uuid = :uuid");

    FileResource fileResource = dbi.withHandle(h -> h.createQuery(query.toString())
        .bind("uuid", uuid)
        //        .mapToBean(FileResourceImpl.class)
        .map(new FileResourceMapper())
        .findOnly());

    if (fileResource instanceof ApplicationFileResource) {
      // no special fields, yet
    } else if (fileResource instanceof AudioFileResource) {
      int result = dbi.withHandle(h -> h.createQuery("SELECT duration FROM fileresources_audio WHERE uuid = :uuid")
          .bind("uuid", uuid)
          .mapTo(Integer.class)
          .findOnly());
      ((AudioFileResource) fileResource).setDuration(result);
    } else if (fileResource instanceof ImageFileResource) {
      Map<String, Object> result = dbi.withHandle(h -> h.createQuery("SELECT width, height FROM fileresources_image WHERE uuid = :uuid")
          .bind("uuid", uuid)
          .mapToMap()
          .findOnly());
      ((ImageFileResource) fileResource).setWidth((int) result.get("width"));
      ((ImageFileResource) fileResource).setHeight((int) result.get("height"));
    } else if (fileResource instanceof TextFileResource) {
      // no special fields, yet
    } else if (fileResource instanceof VideoFileResource) {
      int result = dbi.withHandle(h -> h.createQuery("SELECT duration FROM fileresources_video WHERE uuid = :uuid")
          .bind("uuid", uuid)
          .mapTo(Integer.class)
          .findOnly());
      ((VideoFileResource) fileResource).setDuration(result);
    }

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

    return findOne(fileResource.getUuid());
  }

  @Override
  public FileResource save(FileResource fileResource, InputStream binaryData) {
    try {
      long size = fileResourceRepository.write(fileResource, binaryData);
      fileResource.setSizeInBytes(size);
    } catch (ResourceIOException ex) {
      LOGGER.error("Error saving binary data of fileresource " + fileResource.getUuid().toString(), ex);
    }
    return save(fileResource);
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
          .findOnly());
    } else if (fileResource instanceof AudioFileResource) {
      result = dbi.withHandle(h -> h.createQuery("UPDATE fileresources_audio SET " + baseColumnsSql + ", duration=:duration WHERE uuid=:uuid RETURNING *")
          .bindBean(fileResource)
          .mapToBean(AudioFileResourceImpl.class)
          .findOnly());
    } else if (fileResource instanceof ImageFileResource) {
      result = dbi.withHandle(h -> h.createQuery("UPDATE fileresources_image SET " + baseColumnsSql + ", width=:width, height=:height WHERE uuid=:uuid RETURNING *")
          .bindBean(fileResource)
          .mapToBean(ImageFileResourceImpl.class)
          .findOnly());
    } else if (fileResource instanceof TextFileResource) {
      // no special columns
      result = dbi.withHandle(h -> h.createQuery("UPDATE fileresources_text SET " + baseColumnsSql + " WHERE uuid=:uuid RETURNING *")
          .bindBean(fileResource)
          .mapToBean(TextFileResourceImpl.class)
          .findOnly());
    } else if (fileResource instanceof VideoFileResource) {
      result = dbi.withHandle(h -> h.createQuery("UPDATE fileresources_video SET " + baseColumnsSql + ", duration=:duration WHERE uuid=:uuid RETURNING *")
          .bindBean(fileResource)
          .mapToBean(VideoFileResourceImpl.class)
          .findOnly());
    } else {
      throw new IllegalArgumentException("unknown file resource type " + fileResource.getMimeType().toString());
    }

    return result;
  }

}
