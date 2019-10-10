package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.VersionRepository;
import de.digitalcollections.model.api.identifiable.Version;
import de.digitalcollections.model.impl.identifiable.VersionImpl;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class VersionRepositoryImpl implements VersionRepository {

  private Jdbi dbi;

  @Autowired
  public VersionRepositoryImpl(Jdbi dbi) {
    this.dbi = dbi;
  }

  @Override
  public Version findOne(UUID uuid) {
    Version version =
        (Version)
            dbi.withHandle(
                h ->
                    h.createQuery("SELECT * FROM versions WHERE uuid = :uuid")
                        .bind("uuid", uuid)
                        .mapToBean(VersionImpl.class)
                        .findOne()
                        .orElse(null));
    return version;
  }

  @Override
  public Version findOneByInstanceversionKey(String instVersionKey) {
    Optional<VersionImpl> version =
        (Optional<VersionImpl>)
            dbi.withHandle(
                h ->
                    h.createQuery(
                            "SELECT * FROM versions WHERE instance_version_key = :instance_version_key")
                        .bind("instance_version_key", instVersionKey)
                        .mapToBean(VersionImpl.class)
                        .findFirst());
    if (version.isPresent()) {
      return version.get();
    } else {
      return null;
    }
  }

  @Override
  public Version save(Version version) {
    version.setUuid(UUID.randomUUID());
    version.setCreated(new Date());

    VersionImpl result =
        dbi.withHandle(
            h ->
                h.createQuery(
                        "INSERT INTO versions(uuid, version_value, type_key, instance_key, instance_version_key, description, created, status) "
                            + "VALUES (:uuid, :versionValue, :typeKey, :instanceKey, :instanceVersionKey, :description, :created, :status) RETURNING *")
                    .bindBean(version)
                    .mapToBean(VersionImpl.class)
                    .findOne()
                    .orElse(null));

    return result;
  }

  @Override
  public Version update(Version version) {
    // digitalObject.setLastModified(LocalDateTime.now());

    Version result =
        dbi.withHandle(
            h ->
                h.createQuery("UPDATE versions SET status=:status WHERE uuid=:uuid RETURNING *")
                    .bindBean(version)
                    .mapToBean(VersionImpl.class)
                    .findOne()
                    .orElse(null));

    return result;
  }
}
