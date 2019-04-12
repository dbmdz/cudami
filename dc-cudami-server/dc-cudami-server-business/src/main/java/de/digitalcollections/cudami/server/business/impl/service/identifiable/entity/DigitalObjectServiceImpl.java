package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.DigitalObjectRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.DigitalObjectService;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for Digital Object handling.
 */
@Service
//@Transactional(readOnly = true)
public class DigitalObjectServiceImpl extends EntityServiceImpl<DigitalObject> implements DigitalObjectService<DigitalObject> {

  private static final Logger LOGGER = LoggerFactory.getLogger(DigitalObjectServiceImpl.class);

  @Autowired
  public DigitalObjectServiceImpl(DigitalObjectRepository<DigitalObject> repository) {
    super(repository);
  }
}
