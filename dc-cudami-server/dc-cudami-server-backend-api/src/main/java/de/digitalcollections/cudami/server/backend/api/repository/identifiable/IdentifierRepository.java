package de.digitalcollections.cudami.server.backend.api.repository.identifiable;

import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.List;
import java.util.UUID;

public interface IdentifierRepository {

  long count();

  PageResponse<Identifier> find(PageRequest pageRequest);

  List<Identifier> find(String searchTerm, int maxResults);

  List<Identifier> findByIdentifiable(UUID identifiableUuid);

  Identifier findOne(String namespace, String id);

  Identifier save(Identifier identifier);

  Identifier update(Identifier identifier);

  void delete(UUID uuid);
}
