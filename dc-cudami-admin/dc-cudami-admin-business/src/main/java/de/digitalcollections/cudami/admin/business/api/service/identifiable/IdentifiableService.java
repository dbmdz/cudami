package de.digitalcollections.cudami.admin.business.api.service.identifiable;

import de.digitalcollections.cudami.admin.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.UUID;
import org.springframework.validation.Errors;

public interface IdentifiableService<I extends Identifiable> {

  long count();

  I create();

  PageResponse<I> find(PageRequest pageRequest);

  I get(UUID uuid);

  I save(I identifiable, Errors results) throws IdentifiableServiceException;

  I update(I identifiable, Errors results) throws IdentifiableServiceException;
}
