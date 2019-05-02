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
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.io.InputStream;
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
    // FIXME
//    identifiableRepository.save(fileResource);

    final String baseColumnsSql = "filename, mimetype, size_in_bytes, uri, uuid";
    final String basePropertiesSql = ":filename, :mimeType, :sizeInBytes, :uri, :uuid";

    if (fileResource instanceof ApplicationFileResource) {
      // no special columns, so no extra table, yet
      dbi.withHandle(h -> h.createUpdate("INSERT INTO fileresources(" + baseColumnsSql + ") VALUES (" + basePropertiesSql + ")")
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
      // no special columns, so no extra table, yet
      dbi.withHandle(h -> h.createUpdate("INSERT INTO fileresources(" + baseColumnsSql + ") VALUES (" + basePropertiesSql + ")")
              .bindBean(fileResource)
              .execute());
    } else if (fileResource instanceof VideoFileResource) {
      dbi.withHandle(h -> h.createUpdate("INSERT INTO fileresources_video(" + baseColumnsSql + ", duration) VALUES (" + basePropertiesSql + ", :duration)")
              .bindBean(fileResource)
              .execute());
    } else {
      // handle everything else as (unspecific) fileresource
      dbi.withHandle(h -> h.createUpdate("INSERT INTO fileresources(" + baseColumnsSql + ") VALUES (" + basePropertiesSql + ")")
              .bindBean(fileResource)
              .execute());
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
  public FileResource update(FileResource fileresource) {
    // FIXME
//    identifiableRepository.update(fileresource);

    // TODO add update mimetype specific (see save/insert method above)

    // do not update/left out from statement: created, uuid
    dbi.withHandle(h -> h.createUpdate("UPDATE fileresources SET filename=:filename, mimetype=:mimeType, size_in_bytes=:sizeInBytes, uri=:uri WHERE uuid=:uuid")
            .bindBean(fileresource)
            .execute());
    return findOne(fileresource.getUuid());
  }

}
