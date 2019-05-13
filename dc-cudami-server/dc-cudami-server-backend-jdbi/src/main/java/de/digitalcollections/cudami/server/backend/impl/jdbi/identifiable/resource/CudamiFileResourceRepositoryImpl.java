package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource;

import de.digitalcollections.commons.file.backend.impl.managed.ManagedFileResourceRepositoryImpl;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.CudamiFileResourceRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.exceptions.ResourceIOException;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
public class CudamiFileResourceRepositoryImpl extends IdentifiableRepositoryImpl<FileResource> implements CudamiFileResourceRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(CudamiFileResourceRepositoryImpl.class);

  protected final Jdbi dbi;
  private final IdentifiableRepository identifiableRepository;

  @Autowired
  ManagedFileResourceRepositoryImpl fileResourceRepository;

  @Autowired
  public CudamiFileResourceRepositoryImpl(Jdbi dbi, @Qualifier("identifiableRepositoryImpl") IdentifiableRepository identifiableRepository) {
    this.dbi = dbi;
    this.identifiableRepository = identifiableRepository;
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM fileresources";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOnly());
    return count;
  }

  @Override
  public PageResponse<FileResource> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder("SELECT f.filename as filename, f.mimetype as mimeType, f.size_in_bytes as sizeInBytes, f.uri as uri,")
        .append(" i.uuid as uuid, i.label as label, i.description as description, i.created as created, i.last_modified as lastModified")
        .append(" FROM fileresources f INNER JOIN identifiables i ON f.uuid=i.uuid");

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
    StringBuilder query = new StringBuilder("SELECT f.filename as filename, f.mimetype as mimeType, f.size_in_bytes as sizeInBytes, f.uri as uri,")
        .append(" i.uuid as uuid, i.label as label, i.description as description, i.created as created, i.last_modified as lastModified")
        .append(" FROM fileresources f INNER JOIN identifiables i ON f.uuid=i.uuid")
        .append(" WHERE f.uuid = :uuid");

    FileResource fileResource = dbi.withHandle(h -> h.createQuery(query.toString())
        .bind("uuid", uuid)
        //        .mapToBean(FileResourceImpl.class)
        .map(new FileResourceMapper())
        .findOnly());
    return fileResource;
  }

  @Override
  public FileResource save(FileResource fileResource) {
    identifiableRepository.save(fileResource);
    dbi.withHandle(h -> h.createUpdate("INSERT INTO fileresources(filename, mimetype, size_in_bytes, uri, uuid) VALUES (:filename, :mimeType, :sizeInBytes, :uri, :uuid)")
        .bindBean(fileResource)
        .execute());
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

    identifiableRepository.save(fileResource);
    dbi.withHandle(h -> h.createUpdate("INSERT INTO fileresources(filename, mimetype, size_in_bytes, uri, uuid) VALUES (:filename, :mimeType, :sizeInBytes, :uri, :uuid)")
        .bindBean(fileResource)
        .execute());
    return findOne(fileResource.getUuid());
  }

  @Override
  public FileResource update(FileResource fileresource) {
    identifiableRepository.update(fileresource);
    // do not update/left out from statement: created, uuid
    dbi.withHandle(h -> h.createUpdate("UPDATE fileresources SET filename=:filename, mimetype=:mimeType, size_in_bytes=:sizeInBytes, uri=:uri WHERE uuid=:uuid")
        .bindBean(fileresource)
        .execute());
    return findOne(fileresource.getUuid());
  }

}
