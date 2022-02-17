package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.DigitalObjectRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.work.ItemRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource.FileResourceMetadataRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource.ImageFileResourceRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.legal.LicenseRepositoryImpl;
import de.digitalcollections.model.filter.FilterCriterion;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.identifiable.entity.agent.AgentBuilder;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocationBuilder;
import de.digitalcollections.model.identifiable.entity.work.Item;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.legal.License;
import de.digitalcollections.model.legal.LicenseBuilder;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.production.CreationInfo;
import de.digitalcollections.model.text.LocalizedText;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.result.RowView;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class DigitalObjectRepositoryImpl extends EntityRepositoryImpl<DigitalObject>
    implements DigitalObjectRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(DigitalObjectRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "do";
  public static final String TABLE_ALIAS = "d";
  public static final String TABLE_NAME = "digitalobjects";

  private static final String SQL_SELECT_ALL_FIELDS_JOINS =
      " LEFT JOIN "
          + LicenseRepositoryImpl.TABLE_NAME
          + " AS "
          + LicenseRepositoryImpl.TABLE_ALIAS
          + " ON "
          + LicenseRepositoryImpl.TABLE_ALIAS
          + ".uuid = "
          + TABLE_ALIAS
          + ".license_uuid";

  public static String getSqlInsertFields() {
    return EntityRepositoryImpl.getSqlInsertFields()
        + ", creation_geolocation_uuid"
        + ", creation_date"
        + ", creation_creator_uuid"
        + ", item_uuid"
        + ", license_uuid"
        + ", number_binaryresources"
        + ", parent_uuid";
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  public static String getSqlInsertValues() {
    return EntityRepositoryImpl.getSqlInsertValues()
        + ", :creationInfo?.geoLocation?.uuid"
        + ", :creationInfo?.date"
        + ", :creationInfo?.creator?.uuid"
        + ", :item?.uuid"
        + ", :license?.uuid"
        + ", :numberOfBinaryResources"
        + ", :parent?.uuid";
  }

  public static String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    // TODO: add license, version
    //    return getSqlSelectReducedFields(tableAlias, mappingPrefix) + ", "
    //            + tableAlias + ".version " + mappingPrefix + "_version";
    return getSqlSelectReducedFields(tableAlias, mappingPrefix);
  }

  public static String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return EntityRepositoryImpl.getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".license_uuid "
        + mappingPrefix
        + "_license_uuid"
        + ", "
        + LicenseRepositoryImpl.TABLE_ALIAS
        + ".label "
        + LicenseRepositoryImpl.MAPPING_PREFIX
        + "_label"
        + ", "
        + LicenseRepositoryImpl.TABLE_ALIAS
        + ".url "
        + LicenseRepositoryImpl.MAPPING_PREFIX
        + "_url"
        + ", "
        + tableAlias
        + ".creation_creator_uuid "
        + mappingPrefix
        + "_creation_creator_uuid"
        + ", "
        + tableAlias
        + ".creation_date "
        + mappingPrefix
        + "_creation_date"
        + ", "
        + tableAlias
        + ".creation_geolocation_uuid "
        + mappingPrefix
        + "_creation_geolocation_uuid";
  }

  public static String getSqlUpdateFieldValues() {
    return EntityRepositoryImpl.getSqlUpdateFieldValues()
        + ", creation_geolocation_uuid=:creationInfo?.geoLocation?.uuid"
        + ", creation_date=:creationInfo?.date"
        + ", creation_creator_uuid=:creationInfo?.creator?.uuid"
        + ", item_uuid=:item?.uuid"
        + ", license_uuid=:license?.uuid"
        + ", number_binaryresources=:numberOfBinaryResources"
        + ", parent_uuid=:parent?.uuid";
  }

  @Lazy @Autowired private CollectionRepositoryImpl collectionRepositoryImpl;

  @Lazy @Autowired
  private FileResourceMetadataRepositoryImpl<FileResource> fileResourceMetadataRepositoryImpl;

  @Lazy @Autowired private ImageFileResourceRepositoryImpl imageFileResourceRepositoryImpl;

  @Lazy @Autowired private ItemRepositoryImpl itemRepositoryImpl;

  @Lazy @Autowired private ProjectRepositoryImpl projectRepositoryImpl;

  @Autowired
  public DigitalObjectRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        DigitalObject.class,
        getSqlSelectAllFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlSelectReducedFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlInsertFields(),
        getSqlInsertValues(),
        getSqlUpdateFieldValues(),
        SQL_SELECT_ALL_FIELDS_JOINS,
        createAdditionalReduceRowsBiFunction(),
        cudamiConfig.getOffsetForAlternativePaging());
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
  public SearchPageResponse<Collection> getCollections(
      UUID digitalObjectUuid, SearchPageRequest searchPageRequest) {
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
    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("uuid", digitalObjectUuid);

    String searchTerm = searchPageRequest.getQuery();
    if (StringUtils.hasText(searchTerm)) {
      commonSql += " AND " + getCommonSearchSql(tableAliasCollection);
      argumentMappings.put("searchTerm", this.escapeTermForJsonpath(searchTerm));
    }

    StringBuilder innerQuery = new StringBuilder("SELECT cd.sortindex AS idx, *" + commonSql);

    // as filtering has other target object type (collection) than this repository (digitalobject)
    // we have to rename filter field names to target table alias and column names:
    Filtering filtering = searchPageRequest.getFiltering();
    if (filtering != null) {
      List<FilterCriterion> filterCriteria =
          filtering.getFilterCriteria().stream()
              .map(
                  fc -> {
                    fc.setExpression(collectionRepositoryImpl.getColumnName(fc.getExpression()));
                    fc.setNativeExpression(true);
                    return fc;
                  })
              .collect(Collectors.toList());
      filtering.setFilterCriteria(filterCriteria);
    }
    addFiltering(searchPageRequest, innerQuery, argumentMappings);

    String orderBy = null;
    if (searchPageRequest.getSorting() == null) {
      orderBy = "ORDER BY idx ASC";
      innerQuery.append(
          " ORDER BY cd.sortindex"); // must be the column itself to use window functions
    }
    addPageRequestParams(searchPageRequest, innerQuery);

    List<Collection> result =
        collectionRepositoryImpl.retrieveList(
            collectionRepositoryImpl.getSqlSelectReducedFields(),
            innerQuery,
            argumentMappings,
            orderBy);

    StringBuilder countQuery = new StringBuilder("SELECT count(*)" + commonSql);
    addFiltering(searchPageRequest, countQuery, argumentMappings);
    long total = retrieveCount(countQuery, argumentMappings);

    return new SearchPageResponse<>(result, searchPageRequest, total);
  }

  @Override
  public List<FileResource> getFileResources(UUID digitalObjectUuid) {
    final String frTableAlias = fileResourceMetadataRepositoryImpl.getTableAlias();
    final String frTableName = fileResourceMetadataRepositoryImpl.getTableName();
    final String fieldsSql = fileResourceMetadataRepositoryImpl.getSqlSelectReducedFields();
    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT df.sortindex AS idx, * FROM "
                + frTableName
                + " AS "
                + frTableAlias
                + " LEFT JOIN digitalobject_fileresources AS df ON "
                + frTableAlias
                + ".uuid = df.fileresource_uuid"
                + " WHERE df.digitalobject_uuid = :uuid"
                + " ORDER BY idx ASC");
    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("uuid", digitalObjectUuid);

    List<FileResource> fileResources =
        fileResourceMetadataRepositoryImpl.retrieveList(
            fieldsSql, innerQuery, argumentMappings, "ORDER BY idx ASC");

    return fileResources;
  }

  @Override
  public List<ImageFileResource> getImageFileResources(UUID digitalObjectUuid) {
    final String frTableAlias = imageFileResourceRepositoryImpl.getTableAlias();
    final String frTableName = imageFileResourceRepositoryImpl.getTableName();
    final String fieldsSql = imageFileResourceRepositoryImpl.getSqlSelectAllFields();
    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT df.sortindex AS idx, * FROM "
                + frTableName
                + " AS "
                + frTableAlias
                + " LEFT JOIN digitalobject_fileresources AS df ON "
                + frTableAlias
                + ".uuid = df.fileresource_uuid"
                + " WHERE df.digitalobject_uuid = :uuid"
                + " ORDER BY idx ASC");
    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("uuid", digitalObjectUuid);

    List<ImageFileResource> fileResources =
        imageFileResourceRepositoryImpl.retrieveList(
            fieldsSql, innerQuery, argumentMappings, "ORDER BY idx ASC");

    return fileResources;
  }

  @Override
  public Item getItem(UUID digitalObjectUuid) {
    final String itTableAlias = itemRepositoryImpl.getTableAlias();
    String sqlAdditionalJoins =
        " LEFT JOIN item_digitalobjects AS ido ON " + itTableAlias + ".uuid = ido.item_uuid";

    Filtering filtering =
        Filtering.defaultBuilder()
            .filterNative("ido.digitalobject_uuid")
            .isEquals(digitalObjectUuid)
            .build();

    Item result =
        itemRepositoryImpl.retrieveOne(
            itemRepositoryImpl.getSqlSelectReducedFields(), sqlAdditionalJoins, filtering);
    return result;
  }

  @Override
  public List<Locale> getLanguagesOfCollections(UUID uuid) {
    String collectionTable = this.collectionRepositoryImpl.getTableName();
    String collectionAlias = this.collectionRepositoryImpl.getTableAlias();

    String sql =
        "SELECT DISTINCT jsonb_object_keys("
            + collectionAlias
            + ".label) as languages"
            + " FROM "
            + collectionTable
            + " AS "
            + collectionAlias
            + " LEFT JOIN collection_digitalobjects AS cd ON "
            + collectionAlias
            + ".uuid = cd.collection_uuid"
            + " WHERE cd.digitalobject_uuid = :uuid";
    return this.dbi.withHandle(
        h -> h.createQuery(sql).bind("uuid", uuid).mapTo(Locale.class).list());
  }

  @Override
  public List<Locale> getLanguagesOfProjects(UUID uuid) {
    String projectTable = this.projectRepositoryImpl.getTableName();
    String projectAlias = this.projectRepositoryImpl.getTableAlias();

    String sql =
        "SELECT DISTINCT jsonb_object_keys("
            + projectAlias
            + ".label) as languages"
            + " FROM "
            + projectTable
            + " AS "
            + projectAlias
            + " LEFT JOIN project_digitalobjects AS pd ON "
            + projectAlias
            + ".uuid = pd.project_uuid"
            + " WHERE pd.digitalobject_uuid = :uuid";
    return this.dbi.withHandle(
        h -> h.createQuery(sql).bind("uuid", uuid).mapTo(Locale.class).list());
  }

  @Override
  public SearchPageResponse<Project> getProjects(
      UUID digitalObjectUuid, SearchPageRequest searchPageRequest) {
    final String prTableAlias = projectRepositoryImpl.getTableAlias();
    final String prTableName = projectRepositoryImpl.getTableName();

    String commonSql =
        " FROM "
            + prTableName
            + " AS "
            + prTableAlias
            + " INNER JOIN project_digitalobjects AS pd ON "
            + prTableAlias
            + ".uuid = pd.project_uuid"
            + " WHERE pd.digitalobject_uuid = :uuid";
    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("uuid", digitalObjectUuid);

    String searchTerm = searchPageRequest.getQuery();
    if (StringUtils.hasText(searchTerm)) {
      commonSql += " AND " + getCommonSearchSql(prTableAlias);
      argumentMappings.put("searchTerm", this.escapeTermForJsonpath(searchTerm));
    }

    StringBuilder innerQuery = new StringBuilder("SELECT pd.sortindex AS idx, *" + commonSql);
    addFiltering(searchPageRequest, innerQuery, argumentMappings);

    String orderBy = null;
    if (searchPageRequest.getSorting() == null) {
      orderBy = "ORDER BY idx ASC";
      innerQuery.append(
          " ORDER BY pd.sortindex"); // must be the column itself to use window functions
    }
    addPageRequestParams(searchPageRequest, innerQuery);

    List<Project> result =
        projectRepositoryImpl.retrieveList(
            projectRepositoryImpl.getSqlSelectReducedFields(),
            innerQuery,
            argumentMappings,
            orderBy);

    StringBuilder countQuery = new StringBuilder("SELECT count(*)" + commonSql);
    addFiltering(searchPageRequest, countQuery, argumentMappings);
    long total = retrieveCount(countQuery, argumentMappings);

    return new SearchPageResponse<>(result, searchPageRequest, total);
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
          fileResourceMetadataRepositoryImpl.save(fileResource);
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

  private static BiFunction<Map<UUID, DigitalObject>, RowView, Map<UUID, DigitalObject>>
      createAdditionalReduceRowsBiFunction() {
    return (map, rowView) -> {
      DigitalObject digitalObject =
          map.get(rowView.getColumn(MAPPING_PREFIX + "_uuid", UUID.class));

      // Try to fill license subresource with uuid, url and label
      UUID licenseUuid = rowView.getColumn(MAPPING_PREFIX + "_license_uuid", UUID.class);
      String url = rowView.getColumn(LicenseRepositoryImpl.MAPPING_PREFIX + "_url", String.class);
      LocalizedText label =
          rowView.getColumn(LicenseRepositoryImpl.MAPPING_PREFIX + "_label", LocalizedText.class);
      final License license =
          new LicenseBuilder().withUuid(licenseUuid).withLabel(label).withUrl(url).build();
      if (licenseUuid != null) {
        digitalObject.setLicense(license);
      }

      // Try to fill UUID of geolocation of creator
      UUID creationCreatorUuid =
          rowView.getColumn(MAPPING_PREFIX + "_creation_creator_uuid", UUID.class);
      LocalDate creationDate =
          rowView.getColumn(MAPPING_PREFIX + "_creation_date", LocalDate.class);
      UUID creationGeolocationUuid =
          rowView.getColumn(MAPPING_PREFIX + "_creation_geolocation_uuid", UUID.class);

      // If and oy creation.creator.uuid, creation.geolocation.uuid or creation.date is set,
      // We must build the CreationInfo object
      if (creationCreatorUuid != null || creationDate != null || creationGeolocationUuid != null) {
        CreationInfo creationInfo = new CreationInfo();
        if (creationCreatorUuid != null) {
          creationInfo.setCreator(new AgentBuilder().withUuid(creationCreatorUuid).build());
        }
        if (creationDate != null) {
          creationInfo.setDate(creationDate);
        }
        if (creationGeolocationUuid != null) {
          creationInfo.setGeoLocation(
              new GeoLocationBuilder().withUuid(creationGeolocationUuid).build());
        }
        digitalObject.setCreationInfo(creationInfo);
      }

      return map;
    };
  }

  public void setCollectionRepository(CollectionRepositoryImpl collectionRepositoryImpl) {
    this.collectionRepositoryImpl = collectionRepositoryImpl;
  }

  public void setFileResourceMetadataRepository(
      FileResourceMetadataRepositoryImpl<FileResource> fileResourceMetadataRepositoryImpl) {
    this.fileResourceMetadataRepositoryImpl = fileResourceMetadataRepositoryImpl;
  }
}
