package de.digitalcollections.cudami.server.business.impl.service.identifiable.agent;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.agent.GivenNameRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.agent.GivenNameService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.impl.service.identifiable.IdentifiableServiceImpl;
import de.digitalcollections.model.identifiable.agent.GivenName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GivenNameServiceImpl extends IdentifiableServiceImpl<GivenName>
    implements GivenNameService {

  private static final Logger LOGGER = LoggerFactory.getLogger(GivenNameServiceImpl.class);

  @Autowired
  public GivenNameServiceImpl(
      GivenNameRepository repository,
      IdentifierRepository identifierRepository,
      UrlAliasService urlAliasService) {
    super(repository, identifierRepository, urlAliasService);
  }
}
