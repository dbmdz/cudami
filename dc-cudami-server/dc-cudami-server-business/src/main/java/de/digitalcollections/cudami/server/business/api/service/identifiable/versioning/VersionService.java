package de.digitalcollections.cudami.server.business.api.service.identifiable.versioning;

import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.versioning.Version;
import java.util.UUID;

public interface VersionService {

  Version create(String instancekey, String instanceVersionkey);

  String extractInstanceVersionkey(Identifiable identifiable);

  Version getByInstanceversionKey(String instanceVersionkey);

  Version getByUuid(UUID uuid);

  Version save(Version version) throws Exception;

  Version update(Version version) throws Exception;
}
