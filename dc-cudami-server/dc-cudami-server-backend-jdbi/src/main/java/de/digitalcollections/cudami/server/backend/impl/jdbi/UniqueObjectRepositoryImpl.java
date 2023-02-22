package de.digitalcollections.cudami.server.backend.impl.jdbi;

import de.digitalcollections.cudami.server.backend.api.repository.UniqueObjectRepository;
import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;

public abstract class UniqueObjectRepositoryImpl<U extends UniqueObject>
    extends JdbiRepositoryImpl<U> implements UniqueObjectRepository<U> {

  public UniqueObjectRepositoryImpl(
      Jdbi jdbi,
      String tableName,
      String tableAlias,
      String mappingPrefix,
      int offsetForAlternativePaging) {
    super(jdbi, tableName, tableAlias, mappingPrefix, offsetForAlternativePaging);
  }

  @Override
  public PageResponse find(PageRequest pageRequest) {
    return null;
  }

  @Override
  public U getByUuid(UUID uuid) {
    return UniqueObjectRepository.super.getByUuid(uuid);
  }

  @Override
  public U getByUuidAndFiltering(UUID uuid, Filtering filtering) {
    return null;
  }
}
