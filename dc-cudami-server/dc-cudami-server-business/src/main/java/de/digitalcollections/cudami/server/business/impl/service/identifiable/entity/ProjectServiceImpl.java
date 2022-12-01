package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.ProjectRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.ProjectService;
import de.digitalcollections.cudami.server.config.HookProperties;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

// @Transactional should not be set in derived class to prevent overriding, check base class instead
@Service
public class ProjectServiceImpl extends EntityServiceImpl<Project> implements ProjectService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProjectServiceImpl.class);

  public ProjectServiceImpl(
      ProjectRepository repository,
      IdentifierService identifierService,
      UrlAliasService urlAliasService,
      HookProperties hookProperties,
      LocaleService localeService,
      CudamiConfig cudamiConfig) {
    super(
        repository,
        identifierService,
        urlAliasService,
        hookProperties,
        localeService,
        cudamiConfig);
  }

  @Override
  public boolean addDigitalObjects(UUID projectUuid, List<DigitalObject> digitalObjects) {
    return ((ProjectRepository) repository).addDigitalObjects(projectUuid, digitalObjects);
  }

  @Override
  public boolean delete(UUID uuid) throws ConflictException, IdentifiableServiceException {
    List<DigitalObject> digitalObjects =
        findDigitalObjects(uuid, PageRequest.builder().pageNumber(0).pageSize(1).build())
            .getContent();
    if (!digitalObjects.isEmpty()) {
      throw new ConflictException(
          "Project cannot be deleted, because it has corresponding digital objects!");
    }
    return super.delete(uuid);
  }

  @Override
  public PageResponse<DigitalObject> findDigitalObjects(UUID projectUuid, PageRequest pageRequest) {
    return ((ProjectRepository) repository).findDigitalObjects(projectUuid, pageRequest);
  }

  @Override
  public boolean removeDigitalObject(UUID projectUuid, UUID digitalObjectUuid) {
    return ((ProjectRepository) repository).removeDigitalObject(projectUuid, digitalObjectUuid);
  }

  @Override
  public boolean removeDigitalObjectFromAllProjects(UUID digitalObjectUuid) {
    return ((ProjectRepository) repository).removeDigitalObjectFromAllProjects(digitalObjectUuid);
  }

  @Override
  public boolean setDigitalObjects(UUID projectUuid, List<DigitalObject> digitalObjects) {
    return ((ProjectRepository) repository).setDigitalObjects(projectUuid, digitalObjects);
  }
}
