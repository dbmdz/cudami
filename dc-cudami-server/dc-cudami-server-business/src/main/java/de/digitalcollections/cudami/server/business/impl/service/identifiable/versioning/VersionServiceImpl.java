package de.digitalcollections.cudami.server.business.impl.service.identifiable.versioning;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.versioning.VersionRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.versioning.VersionService;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.versioning.Status;
import de.digitalcollections.model.identifiable.versioning.TypeKey;
import de.digitalcollections.model.identifiable.versioning.Version;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = {Exception.class})
public class VersionServiceImpl implements VersionService {

  @Autowired private VersionRepository repository;

  @Override
  public Version create(String instanceKey, String instanceVersionkey) {
    Version version = new Version();
    version.setInstanceVersionKey(instanceVersionkey);
    version.setInstanceKey(instanceKey);
    version.setStatus(Status.INITIAL);
    version.setTypeKey(TypeKey.DIGITALOBJECT);
    return repository.save(version);
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

  @Override
  public Version getByInstanceversionKey(String instanceVersionkey) {
    return repository.getByInstanceversionKey(instanceVersionkey);
  }

  @Override
  public Version getByUuid(UUID uuid) {
    return repository.getByUuid(uuid);
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
}
