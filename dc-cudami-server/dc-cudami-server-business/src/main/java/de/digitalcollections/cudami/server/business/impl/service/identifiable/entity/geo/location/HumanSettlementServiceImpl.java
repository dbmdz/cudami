package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.geo.location;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.geo.location.HumanSettlementRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.geo.location.HumanSettlementService;
import de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.EntityServiceImpl;
import de.digitalcollections.model.identifiable.entity.geo.location.HumanSettlement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// @Transactional should not be set in derived class to prevent overriding, check base class instead
@Service
public class HumanSettlementServiceImpl extends EntityServiceImpl<HumanSettlement>
    implements HumanSettlementService {

  private static final Logger LOGGER = LoggerFactory.getLogger(HumanSettlementServiceImpl.class);

  @Autowired
  public HumanSettlementServiceImpl(
      HumanSettlementRepository repository,
      IdentifierRepository identifierRepository,
      UrlAliasService urlAliasService) {
    super(repository, identifierRepository, urlAliasService);
  }
}
