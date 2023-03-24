package de.digitalcollections.cudami.server.backend.impl.jdbi;

import de.digitalcollections.cudami.server.backend.api.repository.UniqueObjectRepository;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.model.UniqueObject;
import java.util.List;
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
  public boolean deleteByUuid(UUID uuid) throws RepositoryException {
    // same performance as delete by where uuid = :uuid
    return deleteByUuids(List.of(uuid)) > 0 ? true : false;
  }
}
