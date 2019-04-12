package de.digitalcollections.cudami.admin.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.DigitalObjectRepository;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.entity.DigitalObjectService;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for Digital Object handling.
 */
@Service
//@Transactional(readOnly = true)
public class DigitalObjectServiceImpl extends EntityServiceImpl<DigitalObject> implements DigitalObjectService<DigitalObject> {

  @Autowired
  public DigitalObjectServiceImpl(DigitalObjectRepository<DigitalObject> repository) {
    super(repository);
  }

}
