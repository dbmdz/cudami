package de.digitalcollections.cms.client.business.api.service;

import de.digitalcollections.cms.model.api.security.Operation;
import java.io.Serializable;
import java.util.List;

/**
 * @param <T> domain object
 * @param <ID> unique id
 */
public interface OperationService<T extends Operation, ID extends Serializable> {

  List<T> getAll();
}
