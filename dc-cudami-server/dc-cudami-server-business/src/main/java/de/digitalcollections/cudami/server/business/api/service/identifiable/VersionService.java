package de.digitalcollections.cudami.server.business.api.service.identifiable;

import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.identifiable.Version;
import java.util.UUID;

public interface VersionService {

  Version create(String instancekey, String instanceVersionkey);

  String extractInstanceVersionkey(Identifiable identifiable);

  Version get(UUID uuid);

  Version getByInstanceversionKey(String instanceVersionkey);

  Version save(Version version) throws Exception;

  Version update(Version version) throws Exception;
}
