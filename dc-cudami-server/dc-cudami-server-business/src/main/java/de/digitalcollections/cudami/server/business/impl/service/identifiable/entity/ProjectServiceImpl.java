package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.ProjectRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.ProjectService;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// @Transactional should not be set in derived class to prevent overriding, check base class instead
@Service
public class ProjectServiceImpl extends EntityServiceImpl<Project> implements ProjectService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProjectServiceImpl.class);

  @Autowired
  public ProjectServiceImpl(
      ProjectRepository repository,
      IdentifierRepository identifierRepository,
      UrlAliasService urlAliasService) {
    super(repository, identifierRepository, urlAliasService);
  }

  @Override
  public boolean addDigitalObjects(UUID projectUuid, List<DigitalObject> digitalObjects) {
    return ((ProjectRepository) repository).addDigitalObjects(projectUuid, digitalObjects);
  }

  @Override
  public boolean delete(UUID uuid) {
    // Step 1: Retrieve all identifiers for the project
    Set<Identifier> identifiers;

    Project existingProject = get(uuid);
    if (existingProject != null) {
      identifiers = existingProject.getIdentifiers();
    } else {
      identifiers = Collections.emptySet();
    }

    // Step 2: Delete the project from the repository
    ((ProjectRepository) repository).delete(uuid);

    // Step 3: Delete all identifiers of the project
    for (Identifier identifier : identifiers) {
      identifierRepository.delete(identifier.getUuid());
    }
    return true;
  }

  @Override
  public SearchPageResponse<DigitalObject> getDigitalObjects(
      UUID projectUuid, SearchPageRequest searchPageRequest) {
    return ((ProjectRepository) repository).getDigitalObjects(projectUuid, searchPageRequest);
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
  public boolean saveDigitalObjects(UUID projectUuid, List<DigitalObject> digitalObjects) {
    return ((ProjectRepository) repository).saveDigitalObjects(projectUuid, digitalObjects);
  }
}
