package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.VersionRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.JdbiRepositoryImpl;
import de.digitalcollections.model.api.identifiable.Version;
import de.digitalcollections.model.impl.identifiable.VersionImpl;
import java.util.Date;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class VersionRepositoryImpl extends JdbiRepositoryImpl implements VersionRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(VersionRepositoryImpl.class);

  @Autowired
  public VersionRepositoryImpl(Jdbi dbi) {
    super(dbi, "versions", "v", "ver");
  }

  @Override
  public Version findOne(UUID uuid) {
    final String sql = "SELECT * FROM " + tableName + " WHERE uuid = :uuid";

    Version version
            = dbi.withHandle(
                    h
                    -> h.createQuery(sql)
                            .bind("uuid", uuid)
                            .mapToBean(VersionImpl.class)
                            .findOne()
                            .orElse(null));
    return version;
  }

  @Override
  public Version findOneByInstanceversionKey(String instVersionKey) {
    final String sql = "SELECT * FROM " + tableName + " WHERE instance_version_key = :instance_version_key";

    Version version
            = dbi.withHandle(
                    h
                    -> h.createQuery(sql)
                            .bind("instance_version_key", instVersionKey)
                            .mapToBean(VersionImpl.class)
                            .findFirst()
                            .orElse(null));
    return version;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  protected String getColumnName(String modelProperty) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Version save(Version version) {
    version.setUuid(UUID.randomUUID());
    version.setCreated(new Date());

    final String sql = "INSERT INTO " + tableName + "(uuid, version_value, type_key, instance_key, instance_version_key, description, created, status) "
            + "VALUES (:uuid, :versionValue, :typeKey, :instanceKey, :instanceVersionKey, :description, :created, :status)"
            + " RETURNING *";

    VersionImpl result
            = dbi.withHandle(
                    h
                    -> h.createQuery(sql)
                            .bindBean(version)
                            .mapToBean(VersionImpl.class)
                            .findOne()
                            .orElse(null));

    return result;
  }

  @Override
  public Version update(Version version) {
    // digitalObject.setLastModified(LocalDateTime.now());
    final String sql = "UPDATE " + tableName + " SET status=:status WHERE uuid=:uuid"
            + " RETURNING *";

    Version result
            = dbi.withHandle(
                    h
                    -> h.createQuery(sql)
                            .bindBean(version)
                            .mapToBean(VersionImpl.class)
                            .findOne()
                            .orElse(null));

    return result;
  }
}
