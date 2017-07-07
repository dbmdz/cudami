package de.digitalcollections.cudami.server.business.impl.service;

import de.digitalcollections.cudami.model.api.security.Operation;
import de.digitalcollections.cudami.server.backend.api.repository.OperationRepository;
import de.digitalcollections.cudami.server.business.api.service.OperationService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
//@Transactional(readOnly = true)
public class OperationServiceImpl implements OperationService<Operation, Long> {

  @Autowired
  private OperationRepository operationRepository;

  @Override
  public List<Operation> getAll() {
    return operationRepository.findAll(new Sort(Sort.Direction.ASC, "name"));
  }

  @Override
  public Operation findByName(String name) {
    return operationRepository.findByName(name);
  }
}
