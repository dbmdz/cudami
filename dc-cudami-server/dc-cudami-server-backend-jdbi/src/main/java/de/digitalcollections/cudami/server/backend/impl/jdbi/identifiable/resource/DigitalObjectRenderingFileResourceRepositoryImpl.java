package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.DigitalObjectRenderingFileResourceRepository;
import de.digitalcollections.model.identifiable.resource.FileResource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.springframework.stereotype.Repository;

@Repository
public class DigitalObjectRenderingFileResourceRepositoryImpl
    extends FileResourceMetadataRepositoryImpl<FileResource>
    implements DigitalObjectRenderingFileResourceRepository {

  public DigitalObjectRenderingFileResourceRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
    super(dbi, cudamiConfig);
  }

  @Override
  public List<FileResource> getForDigitalObjectUuid(UUID digitalObjectUuid) {
    final String rfrTableAlias = getTableAlias();
    final String rfrTableName = getTableName();
    final String fieldsSql = getSqlSelectAllFields();
    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT dr.sortindex as idx, * FROM "
                + rfrTableName
                + " AS "
                + rfrTableAlias
                + " INNER JOIN digitalobject_renderingresources AS dr ON "
                + rfrTableAlias
                + ".uuid = dr.fileresource_uuid"
                + " WHERE dr.digitalobject_uuid = :uuid"
                + " ORDER by idx ASC");
    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("uuid", digitalObjectUuid);

    List<FileResource> fileResources =
        retrieveList(fieldsSql, innerQuery, argumentMappings, "ORDER BY idx ASC");

    return fileResources;
  }

  @Override
  public List<FileResource> saveForDigitalObjectUuid(
      UUID digitalObjectUuid, List<FileResource> renderingResources) {
    // as we store the whole list new: delete old entries
    dbi.withHandle(
        h ->
            h.createUpdate(
                    "DELETE FROM digitalobject_renderingresources WHERE digitalobject_uuid = :uuid")
                .bind("uuid", digitalObjectUuid)
                .execute());

    if (renderingResources != null) {
      // first save rendering resources
      for (FileResource renderingResource : renderingResources) {
        if (renderingResource.getUuid() == null) {
          save(renderingResource);
        }
      }

      // second: save relations to digital object
      dbi.useHandle(
          handle -> {
            PreparedBatch preparedBatch =
                handle.prepareBatch(
                    "INSERT INTO digitalobject_renderingresources(digitalobject_uuid, fileresource_uuid, sortIndex) VALUES(:uuid, :fileResourceUuid, :sortIndex)");
            for (FileResource renderingResource : renderingResources) {
              preparedBatch
                  .bind("uuid", digitalObjectUuid)
                  .bind("fileResourceUuid", renderingResource.getUuid())
                  .bind("sortIndex", getIndex(renderingResources, renderingResource))
                  .add();
            }
            preparedBatch.execute();
          });
    }
    return getForDigitalObjectUuid(digitalObjectUuid);
  }
}
