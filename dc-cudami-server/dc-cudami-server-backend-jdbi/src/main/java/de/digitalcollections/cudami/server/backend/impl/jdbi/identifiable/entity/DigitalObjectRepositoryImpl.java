package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.DigitalObjectRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.work.ItemRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource.FileResourceMetadataRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource.ImageFileResourceRepositoryImpl;
import de.digitalcollections.model.api.filter.FilterCriterion;
import de.digitalcollections.model.api.filter.FilterValuePlaceholder;
import de.digitalcollections.model.api.filter.Filtering;
import de.digitalcollections.model.api.identifiable.entity.Collection;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.entity.Project;
import de.digitalcollections.model.api.identifiable.entity.work.Item;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.DigitalObjectImpl;
import de.digitalcollections.model.impl.identifiable.resource.FileResourceImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

@Repository
public class DigitalObjectRepositoryImpl extends EntityRepositoryImpl<DigitalObject>
    implements DigitalObjectRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(DigitalObjectRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "do";
  public static final String TABLE_ALIAS = "d";
  public static final String TABLE_NAME = "digitalobjects";

  public static String getSqlInsertFields() {
    return EntityRepositoryImpl.getSqlInsertFields();
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  public static String getSqlInsertValues() {
    return EntityRepositoryImpl.getSqlInsertValues();
  }

  public static String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    // TODO: add license, version
    //    return getSqlSelectReducedFields(tableAlias, mappingPrefix) + ", "
    //            + tableAlias + ".version " + mappingPrefix + "_version";
    return getSqlSelectReducedFields(tableAlias, mappingPrefix);
  }

  public static String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return EntityRepositoryImpl.getSqlSelectReducedFields(tableAlias, mappingPrefix);
  }

  public static String getSqlUpdateFieldValues() {
    return EntityRepositoryImpl.getSqlUpdateFieldValues();
  }

  @Lazy @Autowired private CollectionRepositoryImpl collectionRepositoryImpl;

  @Lazy @Autowired
  private FileResourceMetadataRepositoryImpl<FileResource> fileResourceMetadataRepositoryImpl;

  @Lazy @Autowired private ImageFileResourceRepositoryImpl imageFileResourceRepositoryImpl;

  @Lazy @Autowired private ItemRepositoryImpl itemRepositoryImpl;

  @Lazy @Autowired private ProjectRepositoryImpl projectRepositoryImpl;

  @Autowired
  public DigitalObjectRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository) {
    super(
        dbi,
        identifierRepository,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        DigitalObjectImpl.class,
        getSqlSelectAllFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlSelectReducedFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlInsertFields(),
        getSqlInsertValues(),
        getSqlUpdateFieldValues());
  }

  @Override
  public void deleteFileResources(UUID digitalObjectUuid) {
    dbi.withHandle(
        h ->
            h.createUpdate(
                    "DELETE FROM digitalobject_fileresources WHERE digitalobject_uuid = :uuid")
                .bind("uuid", digitalObjectUuid)
                .execute());
  }

  @Override
  public PageResponse<Collection> getCollections(UUID digitalObjectUuid, PageRequest pageRequest) {
    final String tableAliasCollection = collectionRepositoryImpl.getTableAlias();
    final String tableNameCollection = collectionRepositoryImpl.getTableName();

    String commonSql =
        " FROM "
            + tableNameCollection
            + " AS "
            + tableAliasCollection
            + " LEFT JOIN collection_digitalobjects AS cd ON "
            + tableAliasCollection
            + ".uuid = cd.collection_uuid"
            + " WHERE cd.digitalobject_uuid = :uuid";

    StringBuilder innerQuery = new StringBuilder("SELECT *" + commonSql);

    // as filtering has other target object type (collection) than this repository (digitalobject)
    // we have to rename filter field names to target table alias and column names:
    Filtering filtering = pageRequest.getFiltering();
    if (filtering != null) {
      List<FilterCriterion> filterCriteria =
          filtering.getFilterCriteria().stream()
              .map(
                  fc -> {
                    fc.setFieldName(collectionRepositoryImpl.getColumnName(fc.getFieldName()));
                    return fc;
                  })
              .collect(Collectors.toList());
      filtering.setFilterCriteria(filterCriteria);
    }
    addFiltering(pageRequest, innerQuery);
    pageRequest.setSorting(null);
    innerQuery.append(" ORDER BY ").append(tableAliasCollection).append(".label ASC");
    addPageRequestParams(pageRequest, innerQuery);

    List<Collection> result =
        collectionRepositoryImpl.retrieveList(
            collectionRepositoryImpl.getSqlSelectReducedFields(),
            innerQuery,
            Map.of("uuid", digitalObjectUuid));

    StringBuilder countQuery = new StringBuilder("SELECT count(*)" + commonSql);
    addFiltering(pageRequest, countQuery);
    long total = retrieveCount(countQuery, Map.of("uuid", digitalObjectUuid));

    return new PageResponseImpl<>(result, pageRequest, total);
  }

  @Override
  public List<FileResource> getFileResources(UUID digitalObjectUuid) {
    final String frTableAlias = fileResourceMetadataRepositoryImpl.getTableAlias();
    final String frTableName = fileResourceMetadataRepositoryImpl.getTableName();
    final String fieldsSql = fileResourceMetadataRepositoryImpl.getSqlSelectReducedFields();
    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT * FROM "
                + frTableName
                + " AS "
                + frTableAlias
                + " LEFT JOIN digitalobject_fileresources AS df ON "
                + frTableAlias
                + ".uuid = df.fileresource_uuid"
                + " WHERE df.digitalobject_uuid = :uuid"
                + " ORDER BY df.sortIndex ASC");
    Map<String, Object> argumentMappings = Map.of("uuid", digitalObjectUuid);

    List<FileResource> fileResources =
        fileResourceMetadataRepositoryImpl.retrieveList(fieldsSql, innerQuery, argumentMappings);

    return fileResources;
  }

  @Override
  public List<ImageFileResource> getImageFileResources(UUID digitalObjectUuid) {
    final String frTableAlias = imageFileResourceRepositoryImpl.getTableAlias();
    final String frTableName = imageFileResourceRepositoryImpl.getTableName();
    final String fieldsSql = imageFileResourceRepositoryImpl.getSqlSelectAllFields();
    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT * FROM "
                + frTableName
                + " AS "
                + frTableAlias
                + " LEFT JOIN digitalobject_fileresources AS df ON "
                + frTableAlias
                + ".uuid = df.fileresource_uuid"
                + " WHERE df.digitalobject_uuid = :uuid"
                + " ORDER BY df.sortIndex ASC");
    Map<String, Object> argumentMappings = Map.of("uuid", digitalObjectUuid);

    List<ImageFileResource> fileResources =
        imageFileResourceRepositoryImpl.retrieveList(fieldsSql, innerQuery, argumentMappings);

    return fileResources;
  }

  @Override
  public Item getItem(UUID digitalObjectUuid) {
    final String itTableAlias = itemRepositoryImpl.getTableAlias();
    String sqlAdditionalJoins =
        " LEFT JOIN item_digitalobjects AS ido ON " + itTableAlias + ".uuid = ido.item_uuid";

    Filtering filtering =
        Filtering.defaultBuilder()
            .filter("ido.digitalobject_uuid")
            .isEquals(new FilterValuePlaceholder(":uuid"))
            .build();

    Item result =
        itemRepositoryImpl.retrieveOne(
            itemRepositoryImpl.getSqlSelectReducedFields(),
            sqlAdditionalJoins,
            filtering,
            Map.of("uuid", digitalObjectUuid));
    return result;
  }

  @Override
  public PageResponse<Project> getProjects(UUID digitalObjectUuid, PageRequest pageRequest) {
    final String prTableAlias = projectRepositoryImpl.getTableAlias();
    final String prTableName = projectRepositoryImpl.getTableName();

    String commonSql =
        " FROM "
            + prTableName
            + " AS "
            + prTableAlias
            + " LEFT JOIN project_digitalobjects AS pd ON "
            + prTableAlias
            + ".uuid = pd.project_uuid"
            + " WHERE pd.digitalobject_uuid = :uuid";

    StringBuilder innerQuery = new StringBuilder("SELECT *" + commonSql);
    addFiltering(pageRequest, innerQuery);
    pageRequest.setSorting(null);
    innerQuery.append(" ORDER BY ").append(prTableAlias).append(".label ASC");
    addPageRequestParams(pageRequest, innerQuery);

    List<Project> result =
        projectRepositoryImpl.retrieveList(
            projectRepositoryImpl.getSqlSelectReducedFields(),
            innerQuery,
            Map.of("uuid", digitalObjectUuid));

    StringBuilder countQuery = new StringBuilder("SELECT count(*)" + commonSql);
    addFiltering(pageRequest, countQuery);
    long total = retrieveCount(countQuery, Map.of("uuid", digitalObjectUuid));

    return new PageResponseImpl<>(result, pageRequest, total);
  }

  @Override
  public DigitalObject save(DigitalObject digitalObject) {
    super.save(digitalObject);

    // for now we implement first interesting use case: new digital object with new fileresources...
    final List<FileResource> fileResources = digitalObject.getFileResources();
    saveFileResources(digitalObject, fileResources);

    DigitalObject result = findOne(digitalObject.getUuid());
    return result;
  }

  @Override
  public List<FileResource> saveFileResources(
      UUID digitalObjectUuid, List<FileResource> fileResources) {

    // as we store the whole list new: delete old entries
    dbi.withHandle(
        h ->
            h.createUpdate(
                    "DELETE FROM digitalobject_fileresources WHERE digitalobject_uuid = :uuid")
                .bind("uuid", digitalObjectUuid)
                .execute());

    if (fileResources != null) {
      // first save fileresources
      for (FileResource fileResource : fileResources) {
        if (fileResource.getUuid() == null) {
          fileResourceMetadataRepositoryImpl.save((FileResourceImpl) fileResource);
        }
      }

      // second: save relations to digital object
      dbi.useHandle(
          handle -> {
            PreparedBatch preparedBatch =
                handle.prepareBatch(
                    "INSERT INTO digitalobject_fileresources(digitalobject_uuid, fileresource_uuid, sortIndex) VALUES(:uuid, :fileResourceUuid, :sortIndex)");
            for (FileResource fileResource : fileResources) {
              preparedBatch
                  .bind("uuid", digitalObjectUuid)
                  .bind("fileResourceUuid", fileResource.getUuid())
                  .bind("sortIndex", getIndex(fileResources, fileResource))
                  .add();
            }
            preparedBatch.execute();
          });
    }
    return getFileResources(digitalObjectUuid);
  }

  @Override
  public DigitalObject update(DigitalObject digitalObject) {
    super.update(digitalObject);
    DigitalObject result = findOne(digitalObject.getUuid());
    return result;
  }
}
