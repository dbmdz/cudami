package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.DigitalObjectRenderingFileResourceRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.JdbiRepositoryImpl;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.resource.FileResourceType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class DigitalObjectRenderingFileResourceRepositoryImpl extends JdbiRepositoryImpl
    implements DigitalObjectRenderingFileResourceRepository {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(DigitalObjectRenderingFileResourceRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "dorr";
  public static final String TABLE_ALIAS = "do_rr";
  public static final String TABLE_NAME = "digitalobject_renderingresources";

  private FileResourceMetadataRepositoryImpl<FileResource> fileResourceMetadataRepositoryImpl;

  @Autowired
  public DigitalObjectRenderingFileResourceRepositoryImpl(
      Jdbi dbi,
      CudamiConfig cudamiConfig,
      FileResourceMetadataRepositoryImpl<FileResource> fileResourceMetadataRepositoryImpl) {
    super(
        dbi, TABLE_NAME, TABLE_ALIAS, MAPPING_PREFIX, cudamiConfig.getOffsetForAlternativePaging());
    this.fileResourceMetadataRepositoryImpl = fileResourceMetadataRepositoryImpl;
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
        break;
      default:
        // nop
    }
    return untypedFileResource;
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    return new ArrayList<>(Arrays.asList("digitalobject_uuid", "fileresource_uuid", "sortIndex"));
  }

  @Override
  public String getColumnName(String modelProperty) {
    return null;
  }

  @Override
  public List<FileResource> getRenderingFileResources(UUID digitalObjectUuid) {
    final String fieldsSql = FileResourceMetadataRepositoryImpl.getSqlSelectAllFields("f", "fr");

    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT "
                + getTableAlias()
                + ".sortindex as idx, *"
                + " FROM fileresources AS f"
                + " INNER JOIN "
                + getTableName()
                + " AS "
                + getTableAlias()
                + " ON f.uuid = "
                + getTableAlias()
                + ".fileresource_uuid"
                + " WHERE "
                + getTableAlias()
                + ".digitalobject_uuid = :uuid"
                + " ORDER by "
                + getTableAlias()
                + ".sortindex ASC");
    Map<String, Object> argumentMappings = new HashMap<>(0);
    argumentMappings.put("uuid", digitalObjectUuid);

    List<FileResource> fileResources =
        fileResourceMetadataRepositoryImpl
            .retrieveList(fieldsSql, innerQuery, argumentMappings, "ORDER BY idx ASC")
            .stream()
            .map(f -> fillResourceType(f))
            .collect(Collectors.toList());

    return fileResources;
  }

  @Override
  protected String getUniqueField() {
    return null;
  }

  @Override
  public void removeByDigitalObject(UUID digitalObjectUuid) {
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM " + getTableName() + " WHERE digitalobject_uuid = :uuid")
                .bind("uuid", digitalObjectUuid)
                .execute());
  }

  @Override
  public void saveRenderingFileResources(
      UUID digitalObjectUuid, List<FileResource> renderingResources) {
    dbi.useHandle(
        handle -> {
          PreparedBatch preparedBatch =
              handle.prepareBatch(
                  "INSERT INTO "
                      + getTableName()
                      + "(digitalobject_uuid, fileresource_uuid, sortIndex) VALUES(:uuid, :fileResourceUuid, :sortIndex)");
          for (FileResource renderingResource : renderingResources) {
            preparedBatch
                .bind("uuid", digitalObjectUuid)
                .bind("fileResourceUuid", renderingResource.getUuid())
                .bind(
                    "sortIndex",
                    fileResourceMetadataRepositoryImpl.getIndex(
                        renderingResources, renderingResource))
                .add();
          }
          preparedBatch.execute();
        });
  }

  @Override
  protected boolean supportsCaseSensitivityForProperty(String modelProperty) {
    return false;
  }

  @Override
  public boolean delete(List<UUID> uuids) {
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM " + tableName + " WHERE fileresource_uuid in (<uuids>)")
                .bindList("uuids", uuids)
                .execute());
    return true;
  }

  @Override
  public int countDigitalObjectsForResource(UUID uuid) {
    return dbi.withHandle(
        h ->
            h.createQuery("SELECT count(*) FROM " + tableName + " WHERE fileresource_uuid = :uuid")
                .bind("uuid", uuid)
                .mapTo(Integer.class)
                .findOne()
                .get());
  }
}
