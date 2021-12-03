package de.digitalcollections.cudami.server.business.impl.service.identifiable.agent;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.agent.FamilyNameRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.agent.FamilyNameService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.impl.service.identifiable.IdentifiableServiceImpl;
import de.digitalcollections.model.identifiable.agent.FamilyName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// @Transactional should not be set in derived class to prevent overriding, check base class instead
@Service
public class FamilyNameServiceImpl extends IdentifiableServiceImpl<FamilyName>
    implements FamilyNameService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FamilyNameServiceImpl.class);

  @Autowired
  public FamilyNameServiceImpl(
      FamilyNameRepository repository,
      IdentifierRepository identifierRepository,
      UrlAliasService urlAliasService) {
    super(repository, identifierRepository, urlAliasService);
  }
}
