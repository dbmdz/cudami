package de.digitalcollections.cudami.admin.backend.api.repository.identifiable;

import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.SearchPageRequest;
import de.digitalcollections.model.api.paging.SearchPageResponse;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public interface IdentifiableRepository<I extends Identifiable> {

  long count();

  I create();

  PageResponse<I> find(PageRequest pageRequest);

  SearchPageResponse<I> find(SearchPageRequest searchPageRequest);

  List<I> find(String searchTerm, int maxResults);

  I findOneByIdentifier(String namespace, String id);

  I findOne(Identifier identifier);

  I findOne(UUID uuid);

  I findOne(UUID uuid, Locale locale);

  I save(I identifiable);

  I update(I identifiable);
}
