package de.digitalcollections.cudami.admin.backend.impl.repository.identifiable.entity;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.DigitalObjectRepository;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.DigitalObjectImpl;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class DigitalObjectRepositoryImpl extends EntityRepositoryImpl<DigitalObject> implements DigitalObjectRepository {

  @Autowired
  private DigitalObjectRepositoryEndpoint endpoint;

  @Override
  public long count() {
    return endpoint.count();
  }

  @Override
  public DigitalObject create() {
    return new DigitalObjectImpl();
  }

  @Override
  public PageResponse<DigitalObject> find(PageRequest pageRequest) {
    FindParams f = getFindParams(pageRequest);
    PageResponse<DigitalObject> pageResponse = endpoint.find(f.getPageNumber(), f.getPageSize(), f.getSortField(), f.getSortDirection(), f.getNullHandling());
    return getGenericPageResponse(pageResponse);
  }

  @Override
  public DigitalObject findOne(UUID uuid) {
    return endpoint.findOne(uuid);
  }

  @Override
  public DigitalObject findOne(UUID uuid, Locale locale) {
    return endpoint.findOne(uuid, locale.toString());
  }

  @Override
  public LinkedHashSet<FileResource> getFileResources(DigitalObject digitalObject) {
    return getFileResources(digitalObject.getUuid());
  }

  @Override
  public LinkedHashSet<FileResource> getFileResources(UUID digitalObjectUuid) {
    return endpoint.getFileResources(digitalObjectUuid);
  }

  @Override
  public LinkedHashSet<ImageFileResource> getImageFileResources(UUID uuid) {
    return endpoint.getImageFileResources(uuid);
  }

  @Override
  public DigitalObject save(DigitalObject digitalObject) {
    return endpoint.save(digitalObject);
  }

  @Override
  public LinkedHashSet<FileResource> saveFileResources(DigitalObject digitalObject, LinkedHashSet<FileResource> fileResources) {
    return saveFileResources(digitalObject.getUuid(), fileResources);
  }

  @Override
  public LinkedHashSet<FileResource> saveFileResources(UUID digitalObjectUuid, LinkedHashSet<FileResource> fileResources) {
    return endpoint.saveFileResources(digitalObjectUuid, fileResources);
  }

  @Override
  public DigitalObject update(DigitalObject digitalObject) {
    return endpoint.update(digitalObject.getUuid(), digitalObject);
  }
}
