package de.digitalcollections.cudami.server.business.impl.service.identifiable;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.VersionRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.VersionService;
import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.Version;
import de.digitalcollections.model.api.identifiable.Version.Status;
import de.digitalcollections.model.api.identifiable.Version.TypeKey;
import de.digitalcollections.model.impl.identifiable.VersionImpl;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VersionServiceImpl implements VersionService {

  @Autowired private VersionRepository repository;

  @Override
  public Version get(UUID uuid) {
    return repository.findOne(uuid);
  }

  @Override
  public Version get(String externalKey) {
    return repository.findOneByInstanceversionKey(externalKey);
  }

  @Override
  public Version create(String instanceKey, String instanceVersionkey) {
    VersionImpl version = new VersionImpl();
    version.setInstanceVersionKey(instanceVersionkey);
    version.setInstanceKey(instanceKey);
    version.setStatus(Status.INITIAL);
    version.setTypeKey(TypeKey.DIGITALOBJECT);
    return repository.save(version);
  }

  @Override
  public Version save(Version version) throws Exception {
    return repository.save(version);
  }

  @Override
  public Version update(Version version) throws Exception {
    if (version == null || version.getUuid() == null || version.getStatus() == null) {
      throw new Exception("Version must have a uuid and a status: " + version);
    }
    return repository.update(version);
  }

  @Override
  public String extractInstanceVersionkey(Identifiable identifiable) {
    for (Identifier identifier : identifiable.getIdentifiers()) {
      if ("instanceVersionkey".equals(identifier.getNamespace())) {
        return identifier.getId();
      }
    }
    return null;
    // return "dummy_instance_version_key_7";
  }
}
