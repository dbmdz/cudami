package de.digitalcollections.cudami.server.business.api.service.identifiable;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public interface IdentifiableService<I extends Identifiable> {

  long count();

  PageResponse<I> find(PageRequest pageRequest);

  List<I> find(String searchTerm, int maxResults);

  I get(Identifier identifier);

  I get(UUID uuid);

  I get(UUID uuid, Locale locale) throws IdentifiableServiceException;

  I getByIdentifier(String namespace, String id);

  I save(I identifiable) throws IdentifiableServiceException;

  I update(I identifiable) throws IdentifiableServiceException;
}
