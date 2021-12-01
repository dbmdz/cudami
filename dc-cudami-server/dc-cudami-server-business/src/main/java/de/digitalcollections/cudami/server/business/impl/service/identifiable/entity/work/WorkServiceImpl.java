package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.work;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work.WorkRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.WorkService;
import de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.EntityServiceImpl;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.work.Item;
import de.digitalcollections.model.identifiable.entity.work.Work;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WorkServiceImpl extends EntityServiceImpl<Work> implements WorkService {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkServiceImpl.class);

  @Autowired
  public WorkServiceImpl(
      WorkRepository repository,
      IdentifierRepository identifierRepository,
      UrlAliasService urlAliasService) {
    super(repository, identifierRepository, urlAliasService);
  }

  @Override
  public List<Agent> getCreators(UUID workUuid) {
    return ((WorkRepository) repository).getCreators(workUuid);
  }

  @Override
  public List<Item> getItems(UUID workUuid) {
    return ((WorkRepository) repository).getItems(workUuid);
  }
}
