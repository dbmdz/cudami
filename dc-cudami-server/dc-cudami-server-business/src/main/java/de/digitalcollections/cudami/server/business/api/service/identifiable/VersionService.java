package de.digitalcollections.cudami.server.business.api.service.identifiable;

import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.identifiable.Version;
import java.util.UUID;

public interface VersionService {

  Version get(UUID uuid);

  Version get(String externalKey);

  Version create(String instancekey, String instanceVersionkey);

  Version save(Version version) throws Exception;

  Version update(Version version) throws Exception;

  String extractInstanceVersionkey(Identifiable identifiable);
}
