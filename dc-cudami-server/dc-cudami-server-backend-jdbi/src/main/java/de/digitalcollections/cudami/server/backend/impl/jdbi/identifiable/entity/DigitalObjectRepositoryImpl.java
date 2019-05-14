package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.DigitalObjectRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.CudamiFileResourceRepository;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.DigitalObjectImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class DigitalObjectRepositoryImpl extends EntityRepositoryImpl<DigitalObject> implements DigitalObjectRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(DigitalObjectRepositoryImpl.class);

  private final CudamiFileResourceRepository cudamiFileResourceRepository;

  @Autowired
  public DigitalObjectRepositoryImpl(Jdbi dbi, CudamiFileResourceRepository cudamiFileResourceRepository) {
    super(dbi);
    this.cudamiFileResourceRepository = cudamiFileResourceRepository;
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM digitalobjects";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOnly());
    return count;
  }

  @Override
  public PageResponse<DigitalObject> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder("SELECT " + IDENTIFIABLE_COLUMNS)
        .append(" FROM digitalobjects");

    addPageRequestParams(pageRequest, query);

    List<DigitalObjectImpl> result = dbi.withHandle(h -> h.createQuery(query.toString())
        .mapToBean(DigitalObjectImpl.class)
        .list());
    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public DigitalObject findOne(UUID uuid) {
    String query = "SELECT " + IDENTIFIABLE_COLUMNS
                   + " FROM digitalobjects"
                   + " WHERE uuid = :uuid";

    DigitalObject digitalObject = dbi.withHandle(h -> h.createQuery(query)
        .bind("uuid", uuid)
        .mapToBean(DigitalObjectImpl.class)
        .findOnly());
    return digitalObject;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[]{"last_modified"};
  }

  @Override
  public LinkedHashSet<FileResource> getFileResources(DigitalObject digitalObject) {
    return getFileResources(digitalObject.getUuid());
  }

  @Override
  public LinkedHashSet<FileResource> getFileResources(UUID digitalObjectUuid) {
    // TODO getting uuids and then select each fileresource from table may be to unperformant...
    // but copying whole select from cudamifileresourcerepository and do joins may be overkill, too?
    String query = "SELECT df.fileresource_uuid as uuid"
                   + " FROM digitalobject_fileresources df"
                   + " WHERE df.digitalobject_uuid = :uuid"
                   + " ORDER BY df.sortIndex ASC";

    List<UUID> list = dbi.withHandle(h -> h.createQuery(query)
        .bind("uuid", digitalObjectUuid)
        .mapTo(UUID.class)
        .list());

    LinkedHashSet<FileResource> result = new LinkedHashSet<>();
    if (list.isEmpty()) {
      return result;
    }
    list.forEach((uuid) -> {
      result.add(cudamiFileResourceRepository.findOne(uuid));
    });
    return result;
  }

  @Override
  public DigitalObject save(DigitalObject digitalObject) {
    digitalObject.setUuid(UUID.randomUUID());
    digitalObject.setCreated(LocalDateTime.now());
    digitalObject.setLastModified(LocalDateTime.now());

    DigitalObject result = dbi.withHandle(h -> h
        .createQuery("INSERT INTO digitalobjects(uuid, created, description, identifiable_type, label, last_modified, entity_type) VALUES (:uuid, :created, :description::JSONB, :type, :label::JSONB, :lastModified, :entityType) RETURNING *")
        .bindBean(digitalObject)
        .mapToBean(DigitalObjectImpl.class)
        .findOnly());

    // for now we implement first interesting use case: new digital object with new fileresources...
    final LinkedHashSet<FileResource> fileResources = digitalObject.getFileResources();
    saveFileResources(digitalObject, fileResources);

    return result;
  }

  @Override
  public LinkedHashSet<FileResource> saveFileResources(DigitalObject digitalObject, LinkedHashSet<FileResource> fileResources) {
    if (fileResources == null) {
      return null;
    }
    return saveFileResources(digitalObject.getUuid(), fileResources);
  }

  @Override
  public LinkedHashSet<FileResource> saveFileResources(UUID digitalObjectUuid, LinkedHashSet<FileResource> fileResources) {

    // as we store the whole list new: delete old entries
    dbi.withHandle(h -> h.createUpdate("DELETE FROM digitalobject_fileresources WHERE digitalobject_uuid = :uuid")
        .bind("uuid", digitalObjectUuid).execute());

    if (fileResources != null) {
      for (FileResource fileResource : fileResources) {
        cudamiFileResourceRepository.save(fileResource);
      }

      dbi.useHandle(handle -> {
        PreparedBatch preparedBatch = handle.prepareBatch("INSERT INTO digitalobject_fileresources(digitalobject_uuid, fileresource_uuid, sortIndex) VALUES(:uuid, :fileResourceUuid, :sortIndex)");
        for (FileResource fileResource : fileResources) {
          preparedBatch.bind("uuid", digitalObjectUuid)
              .bind("fileResourceUuid", fileResource.getUuid())
              .bind("sortIndex", getIndex(fileResources, fileResource))
              .add();
        }
        preparedBatch.execute();
      });
    }
    return getFileResources(digitalObjectUuid);
  }

  private int getIndex(LinkedHashSet<FileResource> fileResources, FileResource fileResource) {
    boolean found = false;
    int pos = -1;
    for (Iterator<FileResource> iterator = fileResources.iterator(); iterator.hasNext();) {
      pos = pos + 1;
      FileResource fr = iterator.next();
      if (fr.getUuid().equals(fileResource.getUuid())) {
        found = true;
        break;
      }
    }
    if (found) {
      return pos;
    }
    return -1;
  }

  @Override
  public DigitalObject update(DigitalObject digitalObject) {
    digitalObject.setLastModified(LocalDateTime.now());

    // do not update/left out from statement (not changed since insert): uuid, created, identifiable_type, entity_type
    DigitalObject result = dbi.withHandle(h -> h
        .createQuery("UPDATE digitalobjects SET description=:description::JSONB, label=:label::JSONB, last_modified=:lastModified WHERE uuid=:uuid RETURNING *")
        .bindBean(digitalObject)
        .mapToBean(DigitalObjectImpl.class)
        .findOnly());
    return result;
  }
}
