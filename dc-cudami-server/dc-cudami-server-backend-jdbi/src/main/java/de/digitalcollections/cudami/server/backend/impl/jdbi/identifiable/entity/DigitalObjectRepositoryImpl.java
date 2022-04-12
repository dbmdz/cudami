package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.DigitalObjectRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent.CorporateBodyRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent.PersonRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.geo.location.GeoLocationRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.work.ItemRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource.FileResourceMetadataRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource.ImageFileResourceRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource.LinkedDataFileResourceRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.legal.LicenseRepositoryImpl;
import de.digitalcollections.model.filter.FilterCriterion;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.DigitalObjectBuilder;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.agent.AgentBuilder;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocation;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocationBuilder;
import de.digitalcollections.model.identifiable.entity.work.Item;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.identifiable.resource.LinkedDataFileResource;
import de.digitalcollections.model.legal.License;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.production.CreationInfo;
import java.time.LocalDate;
import java.util.ArrayList;
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
    return getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
        + LicenseRepositoryImpl.TABLE_ALIAS
        + ".uuid "
        + LicenseRepositoryImpl.MAPPING_PREFIX
        + "_uuid"
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
        + LicenseRepositoryImpl.TABLE_ALIAS
        + ".acronym "
        + LicenseRepositoryImpl.MAPPING_PREFIX
        + "_acronym"
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
        + "_creation_geolocation_uuid"
        + ", "
        + tableAlias
        + ".number_binaryresources "
        + mappingPrefix
        + "_number_binaryresources";
  }

  public static String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return EntityRepositoryImpl.getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".parent_uuid "
        + mappingPrefix
        + "_parent_uuid";
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

  @Lazy @Autowired private CorporateBodyRepositoryImpl corporateBodyRepositoryImpl;

  @Lazy @Autowired private EntityRepositoryImpl<Agent> agentEntityRepositoryImpl;

  @Lazy @Autowired
  private FileResourceMetadataRepositoryImpl<FileResource> fileResourceMetadataRepositoryImpl;

  @Lazy @Autowired private EntityRepositoryImpl<GeoLocation> geolocationEntityRepositoryImpl;

  @Lazy @Autowired private GeoLocationRepositoryImpl geoLocationRepositoryImpl;

  @Lazy @Autowired private ImageFileResourceRepositoryImpl imageFileResourceRepositoryImpl;

  @Lazy @Autowired
  private LinkedDataFileResourceRepositoryImpl linkedDataFileResourceRepositoryImpl;

  @Lazy @Autowired private ItemRepositoryImpl itemRepositoryImpl;

  @Lazy @Autowired private PersonRepositoryImpl personRepositoryImpl;

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
            + " INNER JOIN collection_digitalobjects AS cd ON "
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
                + " INNER JOIN digitalobject_fileresources AS df ON "
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
                + " INNER JOIN digitalobject_fileresources AS df ON "
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
  public List<LinkedDataFileResource> getLinkedDataFileResources(UUID digitalObjectUuid) {
    final String ldfrTableAlias = linkedDataFileResourceRepositoryImpl.getTableAlias();
    final String ldfrTableName = linkedDataFileResourceRepositoryImpl.getTableName();
    final String fieldsSql = linkedDataFileResourceRepositoryImpl.getSqlSelectAllFields();
    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT dl.sortindex as idx, * FROM "
                + ldfrTableName
                + " AS "
                + ldfrTableAlias
                + " INNER JOIN digitalobject_linkeddataresources AS dl ON "
                + ldfrTableAlias
                + ".uuid = dl.linkeddata_fileresource_uuid"
                + " WHERE dl.digitalobject_uuid = :uuid"
                + " ORDER by idx ASC");
    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("uuid", digitalObjectUuid);

    List<LinkedDataFileResource> linkedDataFileResources =
        linkedDataFileResourceRepositoryImpl.retrieveList(
            fieldsSql, innerQuery, argumentMappings, "ORDER BY idx ASC");

    return linkedDataFileResources;
  }

  @Override
  public List<FileResource> getRenderingFileResources(UUID digitalObjectUuid) {
    final String rfrTableAlias = fileResourceMetadataRepositoryImpl.getTableAlias();
    final String rfrTableName = fileResourceMetadataRepositoryImpl.getTableName();
    final String fieldsSql = fileResourceMetadataRepositoryImpl.getSqlSelectAllFields();
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
        fileResourceMetadataRepositoryImpl.retrieveList(
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
            + " INNER JOIN collection_digitalobjects AS cd ON "
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
            + " INNER JOIN project_digitalobjects AS pd ON "
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
  protected String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    if (super.getColumnName(modelProperty) != null) {
      return super.getColumnName(modelProperty);
    }
    switch (modelProperty) {
      case "parent.uuid":
        return tableAlias + ".parent_uuid";
      default:
        return null;
    }
  }

  @Override
  /**
   * Returns the fully filled DigitalObject including all of its direct attributes.
   *
   * <p>If a belonging item exists for this DigitalObject, the Item is returned with nothing but its
   * UUID set, and the client is responsible for retrieving the whole item object!
   */
  public DigitalObject getByUuidAndFiltering(UUID uuid, Filtering filtering) {
    DigitalObject digitalObject = super.getByUuidAndFiltering(uuid, filtering);
    fillAttributes(digitalObject);
    return digitalObject;
  }

  @Override
  /**
   * Returns the fully filled DigitalObject including all of its direct attributes.
   *
   * <p>If a belonging item exists for this DigitalObject, the Item is returned with nothing but its
   * UUID set, and the client is responsible for retrieving the whole item object!
   */
  public DigitalObject getByIdentifier(Identifier identifier) {
    DigitalObject digitalObject = super.getByIdentifier(identifier);
    fillAttributes(digitalObject);
    return digitalObject;
  }

  private void fillAttributes(DigitalObject digitalObject) {
    if (digitalObject == null) {
      return;
    }

    UUID uuid = digitalObject.getUuid();

    // Look for linked data file resources. If they exist, fill the DigitalObject
    List<LinkedDataFileResource> linkedDataFileResources = getLinkedDataFileResources(uuid);
    if (linkedDataFileResources != null && !linkedDataFileResources.isEmpty()) {
      digitalObject.setLinkedDataResources(new ArrayList<>(linkedDataFileResources));
    }

    // Look for rendering resources. If they exist, fill the object
    List<FileResource> renderingResources = getRenderingFileResources(uuid);
    if (renderingResources != null && !renderingResources.isEmpty()) {
      digitalObject.setRenderingResources(new ArrayList<>(renderingResources));
    }

    // Fill the previewImage
    UUID previewImageUuid =
        digitalObject.getPreviewImage() != null ? digitalObject.getPreviewImage().getUuid() : null;
    if (previewImageUuid != null) {
      digitalObject.setPreviewImage(imageFileResourceRepositoryImpl.getByUuid(previewImageUuid));
    }

    // If CreationInfo is set, retrieve the UUIDs of agent and place and fill their objects
    CreationInfo creationInfo = digitalObject.getCreationInfo();
    if (creationInfo != null) {
      UUID creatorUuid =
          creationInfo.getCreator() != null ? creationInfo.getCreator().getUuid() : null;
      if (creatorUuid != null) {
        // Can be either a CorporateBody or a Person
        Agent creatorEntity = agentEntityRepositoryImpl.getByUuid(creatorUuid);
        if (creatorEntity != null) {
          switch (creatorEntity.getEntityType()) {
            case CORPORATE_BODY:
              creationInfo.setCreator(corporateBodyRepositoryImpl.getByUuid(creatorUuid));
              break;
            case PERSON:
              creationInfo.setCreator(personRepositoryImpl.getByUuid(creatorUuid));
              break;
            default:
              creationInfo.setCreator(creatorEntity);
          }
        }
      }

      UUID geolocationUuid =
          creationInfo.getGeoLocation() != null ? creationInfo.getGeoLocation().getUuid() : null;
      if (geolocationUuid != null) {
        // Can be a GeoLocation or a HumanSettlement at the moment
        GeoLocation geolocationEntity = geolocationEntityRepositoryImpl.getByUuid(geolocationUuid);
        if (geolocationEntity != null) {
          switch (geolocationEntity.getEntityType()) {
              // FIXME: Why no HUMAN_SETTLEMENT here?
            default:
              creationInfo.setGeoLocation(geoLocationRepositoryImpl.getByUuid(geolocationUuid));
          }
        }
      }

      UUID parentUuid =
          digitalObject.getParent() != null ? digitalObject.getParent().getUuid() : null;
      if (parentUuid != null) {
        DigitalObject parent = getByUuid(parentUuid);
        if (parent != null) {
          digitalObject.setParent(parent);
        }
      }
    }
  }

  @Override
  public DigitalObject save(DigitalObject digitalObject) {
    super.save(digitalObject);

    // save the rendering resources, which are also FileResources
    final List<FileResource> renderingResources = digitalObject.getRenderingResources();
    saveRenderingResources(digitalObject, renderingResources);

    // save the linked data resources
    final List<LinkedDataFileResource> linkedDataResources = digitalObject.getLinkedDataResources();
    saveLinkedDataFileResources(digitalObject, linkedDataResources);

    DigitalObject result = getByUuid(digitalObject.getUuid());
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
  public List<FileResource> saveRenderingResources(
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
          fileResourceMetadataRepositoryImpl.save(renderingResource);
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
    return getRenderingFileResources(digitalObjectUuid);
  }

  @Override
  public List<LinkedDataFileResource> saveLinkedDataFileResources(
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
                  .bind("sortIndex", getIndex(linkedDataFileResources, linkedDataFileResource))
                  .add();
            }
            preparedBatch.execute();
          });
    }
    return getLinkedDataFileResources(digitalObjectUuid);
  }

  @Override
  public DigitalObject update(DigitalObject digitalObject) {
    super.update(digitalObject);

    // save the rendering resources, which are also FileResources
    final List<FileResource> renderingResources = digitalObject.getRenderingResources();
    saveRenderingResources(digitalObject, renderingResources);

    // save the linked data resources
    final List<LinkedDataFileResource> linkedDataResources = digitalObject.getLinkedDataResources();
    saveLinkedDataFileResources(digitalObject, linkedDataResources);

    DigitalObject result = getByUuid(digitalObject.getUuid());
    return result;
  }

  private static BiFunction<Map<UUID, DigitalObject>, RowView, Map<UUID, DigitalObject>>
      createAdditionalReduceRowsBiFunction() {
    return (map, rowView) -> {
      DigitalObject digitalObject =
          map.get(rowView.getColumn(MAPPING_PREFIX + "_uuid", UUID.class));

      // Try to fill license subresource with uuid, url and label
      License license = rowView.getRow(License.class);
      if (license.getUuid() != null) {
        digitalObject.setLicense(license);
      }

      // Try to fill UUID of geolocation of creator
      UUID creationCreatorUuid =
          rowView.getColumn(MAPPING_PREFIX + "_creation_creator_uuid", UUID.class);
      LocalDate creationDate =
          rowView.getColumn(MAPPING_PREFIX + "_creation_date", LocalDate.class);
      UUID creationGeolocationUuid =
          rowView.getColumn(MAPPING_PREFIX + "_creation_geolocation_uuid", UUID.class);

      // If any of creation.creator.uuid, creation.geolocation.uuid or creation.date is set,
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

      // Fill further attributes
      Integer numberOfBinaryResources =
          rowView.getColumn(MAPPING_PREFIX + "_number_binaryresources", Integer.class);
      digitalObject.setNumberOfBinaryResources(
          numberOfBinaryResources != null ? numberOfBinaryResources : 0);

      return map;
    };
  }

  @Override
  protected void extendReducedIdentifiable(Identifiable identifiable, RowView rowView) {
    super.extendReducedIdentifiable(identifiable, rowView);

    if (!(identifiable instanceof DigitalObject)) {
      return;
    }

    // Fill the parent (empty, only with uuid), if present.
    UUID parentUuid = rowView.getColumn(MAPPING_PREFIX + "_parent_uuid", UUID.class);
    if (parentUuid != null) {
      ((DigitalObject) identifiable)
          .setParent(new DigitalObjectBuilder().withUuid(parentUuid).build());
    }
  }

  // --------- repository setters for testing purposes only ----------------------
  public void setAgentEntityRepository(EntityRepositoryImpl<Agent> agentEntityRepositoryImpl) {
    this.agentEntityRepositoryImpl = agentEntityRepositoryImpl;
  }

  public void setCollectionRepository(CollectionRepositoryImpl collectionRepositoryImpl) {
    this.collectionRepositoryImpl = collectionRepositoryImpl;
  }

  public void setCorporateBodyRepository(CorporateBodyRepositoryImpl corporateBodyRepositoryImpl) {
    this.corporateBodyRepositoryImpl = corporateBodyRepositoryImpl;
  }

  public void setFileResourceMetadataRepository(
      FileResourceMetadataRepositoryImpl<FileResource> fileResourceMetadataRepositoryImpl) {
    this.fileResourceMetadataRepositoryImpl = fileResourceMetadataRepositoryImpl;
  }

  public void setGeolocationEntityRepositoryImpl(
      EntityRepositoryImpl<GeoLocation> geolocationEntityRepositoryImpl) {
    this.geolocationEntityRepositoryImpl = geolocationEntityRepositoryImpl;
  }

  public void setGeoLocationRepositoryImpl(GeoLocationRepositoryImpl geoLocationRepositoryImpl) {
    this.geoLocationRepositoryImpl = geoLocationRepositoryImpl;
  }

  public void setLinkedDataFileResourceRepository(
      LinkedDataFileResourceRepositoryImpl linkedDataFileResourceRepositoryImpl) {
    this.linkedDataFileResourceRepositoryImpl = linkedDataFileResourceRepositoryImpl;
  }

  public void setPersonRepository(PersonRepositoryImpl personRepositoryImpl) {
    this.personRepositoryImpl = personRepositoryImpl;
  }
}
