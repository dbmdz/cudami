package de.digitalcollections.cudami.server.backend.api.repository.identifiable.versioning;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.model.identifiable.versioning.Version;
import java.util.UUID;

public interface VersionRepository {

  Version getByUuid(UUID uuid);

  Version getByInstanceversionKey(String externalKey);

  void save(Version version) throws RepositoryException;

  void update(Version version) throws RepositoryException;
}
