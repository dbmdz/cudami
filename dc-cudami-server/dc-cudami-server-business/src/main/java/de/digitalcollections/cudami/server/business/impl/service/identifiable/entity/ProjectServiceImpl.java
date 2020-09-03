package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.ProjectRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.ProjectService;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.entity.Project;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectServiceImpl extends EntityServiceImpl<Project> implements ProjectService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProjectServiceImpl.class);
  private final IdentifierRepository identifierRepository;

  @Autowired
  public ProjectServiceImpl(
      ProjectRepository repository, IdentifierRepository identifierRepository) {
    super(repository);
    this.identifierRepository = identifierRepository;
  }

  @Override
  public boolean addDigitalObject(Project project, DigitalObject digitalObject) {
    return ((ProjectRepository) repository).addDigitalObject(project, digitalObject);
  }

  @Override
  public boolean addDigitalObjects(Project project, List<DigitalObject> digitalObjects) {
    return ((ProjectRepository) repository).addDigitalObjects(project, digitalObjects);
  }

  @Override
  public PageResponse<DigitalObject> getDigitalObjects(Project project, PageRequest pageRequest) {
    return ((ProjectRepository) repository).getDigitalObjects(project, pageRequest);
  }

  @Override
  public boolean removeDigitalObject(Project project, DigitalObject digitalObject) {
    return ((ProjectRepository) repository).removeDigitalObject(project, digitalObject);
  }

  @Override
  public boolean saveDigitalObjects(Project project, List<DigitalObject> digitalObjects) {
    return ((ProjectRepository) repository).saveDigitalObjects(project, digitalObjects);
  }

  @Override
  public void delete(UUID uuid) {
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
  }

  @Override
  public List<Project> getAll() {
    return ((ProjectRepository) repository).getAll();
  }
}
