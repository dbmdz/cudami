package de.digitalcollections.cudami.server.backend.api.repository.identifiable.versioning;

import de.digitalcollections.model.identifiable.versioning.Version;
import java.util.UUID;

public interface VersionRepository {

  Version findOne(UUID uuid);

  Version findOneByInstanceversionKey(String externalKey);

  Version save(Version version);

  Version update(Version version);
}
