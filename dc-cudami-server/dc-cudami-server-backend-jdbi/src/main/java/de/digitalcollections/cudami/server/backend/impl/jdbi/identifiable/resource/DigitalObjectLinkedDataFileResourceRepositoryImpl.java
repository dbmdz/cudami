package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.DigitalObjectLinkedDataFileResourceRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.JdbiRepositoryImpl;
import de.digitalcollections.model.identifiable.resource.LinkedDataFileResource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class DigitalObjectLinkedDataFileResourceRepositoryImpl extends JdbiRepositoryImpl
    implements DigitalObjectLinkedDataFileResourceRepository {

  public static final String MAPPING_PREFIX = "dold";
  public static final String TABLE_ALIAS = "do_ld";
  public static final String TABLE_NAME = "digitalobject_linkeddataresources";

  private final LinkedDataFileResourceRepositoryImpl linkedDataFileResourceRepositoryImpl;

  @Autowired
  public DigitalObjectLinkedDataFileResourceRepositoryImpl(
      Jdbi dbi,
      CudamiConfig cudamiConfig,
      LinkedDataFileResourceRepositoryImpl linkedDataFileResourceRepository) {
    super(
        dbi, TABLE_NAME, TABLE_ALIAS, MAPPING_PREFIX, cudamiConfig.getOffsetForAlternativePaging());
    this.linkedDataFileResourceRepositoryImpl = linkedDataFileResourceRepository;
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    return new ArrayList<>(
        Arrays.asList("digitalobject_uuid", "linkeddata_fileresource_uuid", "sortIndex"));
  }

  @Override
  public String getColumnName(String modelProperty) {
    return null;
  }

  @Override
  public List<LinkedDataFileResource> getLinkedDataFileResources(UUID digitalObjectUuid) {
    final String fieldsSql = linkedDataFileResourceRepositoryImpl.getSqlSelectAllFields("f", "fr");
    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT "
                + getTableAlias()
                + ".sortindex as idx, *"
                + " FROM fileresources_linkeddata AS f"
                + " INNER JOIN "
                + getTableName()
                + " AS "
                + getTableAlias()
                + " ON f.uuid = "
                + getTableAlias()
                + ".linkeddata_fileresource_uuid"
                + " WHERE "
                + getTableAlias()
                + ".digitalobject_uuid = :uuid"
                + " ORDER by "
                + getTableAlias()
                + ".sortindex ASC");
    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("uuid", digitalObjectUuid);

    return linkedDataFileResourceRepositoryImpl.retrieveList(
        fieldsSql, innerQuery, argumentMappings, "ORDER BY idx ASC");
  }

  @Override
  protected String getUniqueField() {
    return null;
  }

  @Override
  public List<LinkedDataFileResource> setLinkedDataFileResources(
      UUID digitalObjectUuid, List<LinkedDataFileResource> linkedDataFileResources) {
    // as we store the whole list new: delete old entries
    dbi.withHandle(
        h ->
            h.createUpdate(
                    "DELETE FROM digitalobject_linkeddataresources WHERE digitalobject_uuid = :uuid")
                .bind("uuid", digitalObjectUuid)
                .execute());

    if (linkedDataFileResources != null) {
      // first save linked data resources
      for (LinkedDataFileResource linkedDataFileResource : linkedDataFileResources) {
        if (linkedDataFileResource.getUuid() == null) {
          linkedDataFileResourceRepositoryImpl.save(linkedDataFileResource);
        }
      }

      // second: save relations to digital object
      dbi.useHandle(
          handle -> {
            PreparedBatch preparedBatch =
                handle.prepareBatch(
                    "INSERT INTO digitalobject_linkeddataresources(digitalobject_uuid, linkeddata_fileresource_uuid, sortIndex) VALUES(:uuid, :linkedDataFileResourceUuid, :sortIndex)");
            for (LinkedDataFileResource linkedDataFileResource : linkedDataFileResources) {
              preparedBatch
                  .bind("uuid", digitalObjectUuid)
                  .bind("linkedDataFileResourceUuid", linkedDataFileResource.getUuid())
                  .bind(
                      "sortIndex",
                      linkedDataFileResourceRepositoryImpl.getIndex(
                          linkedDataFileResources, linkedDataFileResource))
                  .add();
            }
            preparedBatch.execute();
          });
    }
    return getLinkedDataFileResources(digitalObjectUuid);
  }

  @Override
  protected boolean supportsCaseSensitivityForProperty(String modelProperty) {
    return false;
  }

  @Override
  public int delete(List<UUID> uuids) {
    return dbi.withHandle(
        h ->
            h.createUpdate(
                    "DELETE FROM " + tableName + " WHERE linkeddata_fileresource_uuid in (<uuids>)")
                .bindList("uuids", uuids)
                .execute());
  }

  @Override
  public int countDigitalObjectsForResource(UUID uuid) {
    return dbi.withHandle(
        h ->
            h.createQuery(
                    "SELECT count(*) FROM "
                        + tableName
                        + " WHERE linkeddata_fileresource_uuid = :uuid")
                .bind("uuid", uuid)
                .mapTo(Integer.class)
                .findOne()
                .get());
  }
}
