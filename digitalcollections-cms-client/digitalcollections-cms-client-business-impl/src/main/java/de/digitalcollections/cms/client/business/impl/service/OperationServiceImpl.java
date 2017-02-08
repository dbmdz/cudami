package de.digitalcollections.cms.client.business.impl.service;

import de.digitalcollections.cms.client.backend.api.repository.OperationRepository;
import de.digitalcollections.cms.client.business.api.service.OperationService;
import de.digitalcollections.cms.model.api.security.Operation;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OperationServiceImpl implements OperationService<Operation, Long> {

  @Autowired
  private OperationRepository operationRepository;

  @Override
  public List<Operation> getAll() {
    return operationRepository.findAll(new Sort(Sort.Direction.ASC, "name"));
  }

}
