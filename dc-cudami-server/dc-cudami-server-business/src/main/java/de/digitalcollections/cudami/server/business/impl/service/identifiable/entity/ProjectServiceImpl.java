package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.ProjectRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.ProjectService;
import de.digitalcollections.cudami.server.config.HookProperties;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
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
  public boolean addDigitalObject(Project project, DigitalObject digitalObject)
      throws ServiceException {
    try {
      return ((ProjectRepository) repository).addDigitalObject(project, digitalObject);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public boolean addDigitalObjects(Project project, List<DigitalObject> digitalObjects)
      throws ServiceException {
    try {
      return ((ProjectRepository) repository).addDigitalObjects(project, digitalObjects);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public boolean delete(Project project) throws ConflictException, ServiceException {
    long amountDigitalObjects =
        findDigitalObjects(project, PageRequest.builder().pageNumber(0).pageSize(1).build())
            .getTotalElements();
    if (amountDigitalObjects > 0) {
      throw new ConflictException(
          "Project cannot be deleted, because it has corresponding digital objects!");
    }
    return super.delete(project);
  }

  @Override
  public PageResponse<DigitalObject> findDigitalObjects(Project project, PageRequest pageRequest)
      throws ServiceException {
    try {
      return ((ProjectRepository) repository).findDigitalObjects(project, pageRequest);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public boolean removeDigitalObject(Project project, DigitalObject digitalObject)
      throws ServiceException {
    try {
      return ((ProjectRepository) repository).removeDigitalObject(project, digitalObject);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public boolean removeDigitalObjectFromAllProjects(DigitalObject digitalObject)
      throws ServiceException {
    try {
      return ((ProjectRepository) repository).removeDigitalObjectFromAllProjects(digitalObject);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public boolean setDigitalObjects(Project project, List<DigitalObject> digitalObjects)
      throws ServiceException {
    try {
      return ((ProjectRepository) repository).setDigitalObjects(project, digitalObjects);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }
}
