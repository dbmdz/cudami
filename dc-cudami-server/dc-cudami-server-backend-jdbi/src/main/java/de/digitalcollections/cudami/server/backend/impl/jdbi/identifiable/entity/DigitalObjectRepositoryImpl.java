package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.DigitalObjectRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.CudamiFileResourceRepository;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.DigitalObjectImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
public class DigitalObjectRepositoryImpl<D extends DigitalObjectImpl> extends EntityRepositoryImpl<D> implements DigitalObjectRepository<D> {

  private static final Logger LOGGER = LoggerFactory.getLogger(DigitalObjectRepositoryImpl.class);

  private final EntityRepository entityRepository;

  private final CudamiFileResourceRepository cudamiFileResourceRepository;

  @Autowired
  public DigitalObjectRepositoryImpl(
      @Qualifier("identifiableRepositoryImpl") IdentifiableRepository identifiableRepository,
      @Qualifier("entityRepositoryImpl") EntityRepository entityRepository,
      CudamiFileResourceRepository cudamiFileResourceRepository,
      Jdbi dbi) {
    super(dbi, identifiableRepository);
    this.entityRepository = entityRepository;
    this.cudamiFileResourceRepository = cudamiFileResourceRepository;
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM digitalobjects";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOnly());
    return count;
  }

  @Override
  public PageResponse<D> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder("SELECT dio.id as id, i.uuid as uuid, i.label as label, i.description as description")
        .append(" FROM digitalobjects dio INNER JOIN entities e ON dio.uuid=e.uuid INNER JOIN identifiables i ON dio.uuid=i.uuid");

    addPageRequestParams(pageRequest, query);

    List<DigitalObjectImpl> result = dbi.withHandle(h -> h.createQuery(query.toString())
        .mapToBean(DigitalObjectImpl.class)
        .list());
    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public D findOne(UUID uuid) {
    String query = "SELECT dio.id as id, i.uuid as uuid, i.label as label, i.description as description"
                   + " FROM digitalobjects dio INNER JOIN entities e ON dio.uuid=e.uuid INNER JOIN identifiables i ON dio.uuid=i.uuid"
                   + " WHERE i.uuid = :uuid";

    D digitalObject = (D) dbi.withHandle(h -> h.createQuery(query)
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
  public LinkedHashSet<FileResource> getFileResources(D digitalObject) {
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
  public D save(D digitalObject) {
    entityRepository.save(digitalObject);
    dbi.withHandle(h -> h.createUpdate("INSERT INTO digitalobjects(uuid) VALUES (:uuid)")
        .bindBean(digitalObject)
        .execute());

    // for now we implement first interesting use case: new digital object with new fileresources...
    final LinkedHashSet<FileResource> fileResources = digitalObject.getFileResources();
    saveFileResources(digitalObject, fileResources);

    return findOne(digitalObject.getUuid());
  }

  @Override
  public LinkedHashSet<FileResource> saveFileResources(D digitalObject, LinkedHashSet<FileResource> fileResources) {
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
  public D update(D digitalObject) {
    entityRepository.update(digitalObject);
    // do not update/left out from statement: created, uuid
    // TODO set digitalObject fields (until now there are no own fields...)
//    dbi.withHandle(h -> h.createUpdate("UPDATE digitalobjects SET ... WHERE uuid=:uuid")
//            .bindBean(digitalObject)
//            .execute());
    return findOne(digitalObject.getUuid());
  }
}
