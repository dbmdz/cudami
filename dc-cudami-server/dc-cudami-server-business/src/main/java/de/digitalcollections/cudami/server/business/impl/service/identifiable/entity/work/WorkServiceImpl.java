package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.work;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work.WorkRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.WorkService;
import de.digitalcollections.cudami.server.business.impl.service.identifiable.IdentifiableServiceImpl;
import de.digitalcollections.model.api.identifiable.entity.agent.Agent;
import de.digitalcollections.model.api.identifiable.entity.work.Item;
import de.digitalcollections.model.api.identifiable.entity.work.Work;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WorkServiceImpl extends IdentifiableServiceImpl<Work> implements WorkService {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkServiceImpl.class);

  @Autowired
  public WorkServiceImpl(WorkRepository repository) {
    super(repository);
  }

  @Override
  public PageResponse<Work> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial) {
    PageResponse<Work> result =
        ((WorkRepository) repository).findByLanguageAndInitial(pageRequest, language, initial);
    return result;
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
