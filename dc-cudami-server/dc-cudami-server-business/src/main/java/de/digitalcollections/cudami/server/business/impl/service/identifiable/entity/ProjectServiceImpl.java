package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.ProjectRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.ProjectService;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.entity.Project;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectServiceImpl extends EntityServiceImpl<Project> implements ProjectService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProjectServiceImpl.class);

  @Autowired
  public ProjectServiceImpl(ProjectRepository repository) {
    super(repository);
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
  public boolean saveDigitalObjects(Project project, List<DigitalObject> digitalObjects) {
    return ((ProjectRepository) repository).saveDigitalObjects(project, digitalObjects);
  }
}
