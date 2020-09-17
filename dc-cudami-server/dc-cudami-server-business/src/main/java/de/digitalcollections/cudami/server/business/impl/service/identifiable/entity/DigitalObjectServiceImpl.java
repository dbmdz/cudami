package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.DigitalObjectRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.DigitalObjectService;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.Collection;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.entity.Project;
import de.digitalcollections.model.api.identifiable.entity.enums.EntityType;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Service for Digital Object handling. */
@Service
public class DigitalObjectServiceImpl extends EntityServiceImpl<DigitalObject>
    implements DigitalObjectService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DigitalObjectServiceImpl.class);

  @Autowired
  public DigitalObjectServiceImpl(DigitalObjectRepository repository) {
    super(repository);
  }

  @Override
  public DigitalObject getByIdentifier(String namespace, String id) {
    return ((DigitalObjectRepository) repository).findByIdentifier(namespace, id);
  }

  @Override
  public PageResponse<Collection> getCollections(
      DigitalObject digitalObject, PageRequest pageRequest) {
    return ((DigitalObjectRepository) repository).getCollections(digitalObject, pageRequest);
  }

  @Override
  public List<FileResource> getFileResources(DigitalObject digitalObject) {
    return getFileResources(digitalObject.getUuid());
  }

  @Override
  public List<FileResource> getFileResources(UUID digitalObjectUuid) {
    return ((DigitalObjectRepository) repository).getFileResources(digitalObjectUuid);
  }

  @Override
  public List<ImageFileResource> getImageFileResources(DigitalObject digitalObject) {
    return getImageFileResources(digitalObject.getUuid());
  }

  @Override
  public List<ImageFileResource> getImageFileResources(UUID digitalObjectUuid) {
    return ((DigitalObjectRepository) repository).getImageFileResources(digitalObjectUuid);
  }

  Identifier getidentifer(DigitalObject digitalObject, String name) {
    for (Identifier identifier : digitalObject.getIdentifiers()) {
      if (name.equals(identifier.getNamespace())) {
        return identifier;
      }
    }
    return null;
  }

  @Override
  public PageResponse<Project> getProjects(DigitalObject digitalObject, PageRequest pageRequest) {
    return ((DigitalObjectRepository) repository).getProjects(digitalObject, pageRequest);
  }

  @Override
  public List<FileResource> saveFileResources(
      DigitalObject digitalObject, List<FileResource> fileResources) {
    return saveFileResources(digitalObject.getUuid(), fileResources);
  }

  @Override
  public List<FileResource> saveFileResources(
      UUID digitalObjectUuid, List<FileResource> fileResources) {
    return ((DigitalObjectRepository) repository)
        .saveFileResources(digitalObjectUuid, fileResources);
  }

  @Override
  public List<DigitalObject> findAllReduced() {
    return ((EntityRepository) repository).findAllReduced(EntityType.DIGITAL_OBJECT);
  }
}
