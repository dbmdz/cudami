package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.agent;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent.AgentRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.agent.AgentService;
import de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.EntityServiceImpl;
import de.digitalcollections.cudami.server.config.HookProperties;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.work.Work;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Service for Agent handling. */
// @Transactional should not be set in derived class to prevent overriding, check base class instead
@Service
public class AgentServiceImpl extends EntityServiceImpl<Agent> implements AgentService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AgentServiceImpl.class);

  public AgentServiceImpl(
      AgentRepository repository,
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
  public Set<DigitalObject> getDigitalObjects(UUID uuidAgent) {
    return ((AgentRepository) repository).getDigitalObjects(uuidAgent);
  }

  @Override
  public Set<Work> getWorks(UUID uuidAgent) {
    return ((AgentRepository) repository).getWorks(uuidAgent);
  }
}
