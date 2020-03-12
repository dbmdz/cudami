package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.DigitalObjectRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.VersionService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.DigitalObjectService;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.ImageFileResource;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Service for Digital Object handling. */
@Service
// @Transactional(readOnly = true)
public class DigitalObjectServiceImpl extends EntityServiceImpl<DigitalObject>
    implements DigitalObjectService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DigitalObjectServiceImpl.class);

  @Autowired VersionService versionService;

  @Autowired
  public DigitalObjectServiceImpl(DigitalObjectRepository repository) {
    super(repository);
  }

  @Override
  public DigitalObject getByIdentifier(String namespace, String id) {
    long start = System.currentTimeMillis();
    DigitalObject digitalObject =
        ((DigitalObjectRepository) repository).findByIdentifier(namespace, id);
    long end = System.currentTimeMillis();
    LOGGER.info("duration : " + (end - start));
    return digitalObject;
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

  //  @Override
  //  public DigitalObject save(DigitalObject digitalObject) throws IdentifiableServiceException {
  //
  //    Version version = null;
  //    if (digitalObject.getVersion() == null) {
  //      String instanceVersionKey = versionService.extractInstanceVersionkey(digitalObject);
  //      if (instanceVersionKey == null) {
  //        throw new RuntimeException("No instanceVersionKey defined for: " + digitalObject);
  //      }
  //      version = versionService.get(instanceVersionKey);
  //      if (version == null) {
  //        Identifier zendIdentifier = getidentifer(digitalObject, "zend");
  //        if (zendIdentifier == null) {
  //          throw new RuntimeException("No zendid defined for: " + digitalObject);
  //        }
  //        String instanceKey = zendIdentifier.getId();
  //        LOGGER.info("Digital object saved: " + digitalObject);
  //        version = versionService.create(instanceKey, instanceVersionKey);
  //        digitalObject.setVersion(version);
  //        return (DigitalObject) repository.save(digitalObject);
  //      }
  //    }
  //
  //    LOGGER.info("Digital object version already stored: " + digitalObject + " : " + version);
  //    // throw new RuntimeException("update digital object not implemented");
  //    // todo find
  //    return digitalObject;
  //  }
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
}
