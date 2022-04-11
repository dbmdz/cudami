package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.DigitalObjectRenderingFileResourceRepository;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.resource.FileResourceType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class DigitalObjectRenderingFileResourceRepositoryImpl
    extends FileResourceMetadataRepositoryImpl<FileResource>
    implements DigitalObjectRenderingFileResourceRepository {

  @Autowired
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
        retrieveList(fieldsSql, innerQuery, argumentMappings, "ORDER BY idx ASC").stream()
            .map(f -> fillResourceType(f))
            .collect(Collectors.toList());

    return fileResources;
  }

  @Override
  public void deleteRelatedRenderingResourcesForDigitalObjectUuid(UUID digitalObjectUuid) {
    dbi.withHandle(
        h ->
            h.createUpdate(
                    "DELETE FROM digitalobject_renderingresources WHERE digitalobject_uuid = :uuid")
                .bind("uuid", digitalObjectUuid)
                .execute());
  }

  @Override
  public void saveRelatedRenderingResourcesForDigitalObjectUuid(
      UUID digitalObjectUuid, List<FileResource> renderingResources) {
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

  private FileResource fillResourceType(FileResource untypedFileResource) {
    switch (untypedFileResource.getMimeType().getPrimaryType()) {
      case "application":
        untypedFileResource.setFileResourceType(FileResourceType.APPLICATION);
        break;
      case "audio":
        untypedFileResource.setFileResourceType(FileResourceType.AUDIO);
        break;
      case "image":
        untypedFileResource.setFileResourceType(FileResourceType.IMAGE);
        break;
      case "text":
        untypedFileResource.setFileResourceType(FileResourceType.TEXT);
        break;
      case "video":
        untypedFileResource.setFileResourceType(FileResourceType.VIDEO);
      default:
        // nop
    }
    return untypedFileResource;
  }
}
