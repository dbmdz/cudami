package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.CorporationRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.CorporationService;
import de.digitalcollections.model.api.identifiable.entity.Corporation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CorporationServiceImpl extends EntityServiceImpl<Corporation>
    implements CorporationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CorporationServiceImpl.class);

  @Autowired
  public CorporationServiceImpl(CorporationRepository repository) {
    super(repository);
  }
}
