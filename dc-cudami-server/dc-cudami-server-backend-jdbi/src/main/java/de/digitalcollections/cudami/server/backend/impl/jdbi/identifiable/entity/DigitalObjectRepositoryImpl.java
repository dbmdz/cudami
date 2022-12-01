package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.DigitalObjectRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent.CorporateBodyRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent.PersonRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.geo.location.GeoLocationRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource.FileResourceMetadataRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource.ImageFileResourceRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource.LinkedDataFileResourceRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.legal.LicenseRepositoryImpl;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.digitalobject.CreationInfo;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocation;
import de.digitalcollections.model.identifiable.entity.item.Item;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.legal.License;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.result.RowView;
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

  private static final String SQL_SELECT_ALL_FIELDS_JOINS =
      " LEFT JOIN "
          + LicenseRepositoryImpl.TABLE_NAME
          + " AS "
          + LicenseRepositoryImpl.TABLE_ALIAS
          + " ON "
          + TABLE_ALIAS
          + ".license_uuid = "
          + LicenseRepositoryImpl.TABLE_ALIAS
          + ".uuid";
  public static final String TABLE_NAME = "digitalobjects";

  private static final BiConsumer<Map<UUID, DigitalObject>, RowView>
      ADDITIONAL_REDUCE_ROWS_BICONSUMER =
          (map, rowView) -> {
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
            if (creationCreatorUuid != null
                || creationDate != null
                || creationGeolocationUuid != null) {
              CreationInfo creationInfo = new CreationInfo();
              if (creationCreatorUuid != null) {
                creationInfo.setCreator(Agent.builder().uuid(creationCreatorUuid).build());
              }
              if (creationDate != null) {
                creationInfo.setDate(creationDate);
              }
              if (creationGeolocationUuid != null) {
                creationInfo.setGeoLocation(
                    GeoLocation.builder().uuid(creationGeolocationUuid).build());
              }
              digitalObject.setCreationInfo(creationInfo);
            }

            Integer numberOfBinaryResources =
                rowView.getColumn(MAPPING_PREFIX + "_number_binaryresources", Integer.class);
            if (numberOfBinaryResources != null) {
              digitalObject.setNumberOfBinaryResources(numberOfBinaryResources);
            }

            // set item UUID only
            UUID itemUuid = rowView.getColumn(MAPPING_PREFIX + "_item_uuid", UUID.class);
            if (itemUuid != null) {
              digitalObject.setItem(Item.builder().uuid(itemUuid).build());
            }
          };

  @Override
  public String getSqlInsertFields() {
    return super.getSqlInsertFields()
        + ", creation_geolocation_uuid"
        + ", creation_date"
        + ", creation_creator_uuid"
        + ", item_uuid"
        + ", license_uuid"
        + ", number_binaryresources"
        + ", parent_uuid";
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  @Override
  public String getSqlInsertValues() {
    return super.getSqlInsertValues()
        + ", :creationInfo?.geoLocation?.uuid"
        + ", :creationInfo?.date"
        + ", :creationInfo?.creator?.uuid"
        + ", :item?.uuid"
        + ", :license?.uuid"
        + ", :numberOfBinaryResources"
        + ", :parent?.uuid";
  }

  @Override
  public String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
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

  @Override
  public String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return super.getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".parent_uuid "
        + mappingPrefix
        + "_parent_uuid"
        + ", "
        + tableAlias
        + ".item_uuid "
        + mappingPrefix
        + "_item_uuid";
  }

  @Override
  public String getSqlUpdateFieldValues() {
    return super.getSqlUpdateFieldValues()
        + ", creation_geolocation_uuid=:creationInfo?.geoLocation?.uuid"
        + ", creation_date=:creationInfo?.date"
        + ", creation_creator_uuid=:creationInfo?.creator?.uuid"
        + ", item_uuid=:item?.uuid"
        + ", license_uuid=:license?.uuid"
        + ", number_binaryresources=:numberOfBinaryResources"
        + ", parent_uuid=:parent?.uuid";
  }

  @Lazy @Autowired private EntityRepositoryImpl<Agent> agentEntityRepositoryImpl;

  @Lazy @Autowired private CollectionRepositoryImpl collectionRepositoryImpl;

  @Lazy @Autowired private CorporateBodyRepositoryImpl corporateBodyRepositoryImpl;

  @Lazy @Autowired
  private FileResourceMetadataRepositoryImpl<FileResource> fileResourceMetadataRepositoryImpl;

  @Lazy @Autowired private GeoLocationRepositoryImpl<GeoLocation> geoLocationRepositoryImpl;
  @Lazy @Autowired private EntityRepositoryImpl<GeoLocation> geolocationEntityRepositoryImpl;

  @Lazy @Autowired private ImageFileResourceRepositoryImpl imageFileResourceRepositoryImpl;

  @Lazy @Autowired
  private LinkedDataFileResourceRepositoryImpl linkedDataFileResourceRepositoryImpl;

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
        SQL_SELECT_ALL_FIELDS_JOINS,
        ADDITIONAL_REDUCE_ROWS_BICONSUMER,
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
  protected void extendReducedIdentifiable(DigitalObject identifiable, RowView rowView) {
    super.extendReducedIdentifiable(identifiable, rowView);

    // Fill the parent (empty, only with uuid), if present.
    UUID parentUuid = rowView.getColumn(MAPPING_PREFIX + "_parent_uuid", UUID.class);
    if (parentUuid != null) {
      identifiable.setParent(DigitalObject.builder().uuid(parentUuid).build());
    }
  }

  private void fillAttributes(DigitalObject digitalObject) {
    if (digitalObject == null) {
      return;
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
          switch (creatorEntity.getIdentifiableObjectType()) {
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
          switch (geolocationEntity.getIdentifiableObjectType()) {
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
  public PageResponse<Collection> findCollections(UUID digitalObjectUuid, PageRequest pageRequest) {
    final String crossTableAlias = "xtable";

    final String collectionTableAlias = collectionRepositoryImpl.getTableAlias();
    final String collectionTableName = collectionRepositoryImpl.getTableName();
    StringBuilder commonSql =
        new StringBuilder(
            " FROM "
                + collectionTableName
                + " AS "
                + collectionTableAlias
                + " INNER JOIN collection_digitalobjects AS "
                + crossTableAlias
                + " ON "
                + collectionTableAlias
                + ".uuid = "
                + crossTableAlias
                + ".collection_uuid"
                + " WHERE "
                + crossTableAlias
                + ".digitalobject_uuid = :uuid");
    Map<String, Object> argumentMappings = new HashMap<>(0);
    argumentMappings.put("uuid", digitalObjectUuid);
    String executedSearchTerm = addSearchTerm(pageRequest, commonSql, argumentMappings);
    Filtering filtering = pageRequest.getFiltering();
    // as filtering has other target object type (collection) than this repository (digitalobject)
    // we have to rename filter field names to target table alias and column names:
    mapFilterExpressionsToOtherTableColumnNames(filtering, collectionRepositoryImpl);
    addFiltering(pageRequest, commonSql, argumentMappings);

    StringBuilder innerQuery =
        new StringBuilder("SELECT " + crossTableAlias + ".sortindex AS idx, * " + commonSql);
    String orderBy = addCrossTablePageRequestParams(pageRequest, innerQuery, crossTableAlias);
    List<Collection> result =
        collectionRepositoryImpl.retrieveList(
            collectionRepositoryImpl.getSqlSelectReducedFields(),
            innerQuery,
            argumentMappings,
            orderBy);

    StringBuilder countQuery = new StringBuilder("SELECT count(*)" + commonSql);
    long total = retrieveCount(countQuery, argumentMappings);

    return new PageResponse<>(result, pageRequest, total, executedSearchTerm);
  }

  @Override
  public PageResponse<Project> findProjects(UUID digitalObjectUuid, PageRequest pageRequest) {
    final String crossTableAlias = "xtable";

    final String prTableAlias = projectRepositoryImpl.getTableAlias();
    final String prTableName = projectRepositoryImpl.getTableName();
    StringBuilder commonSql =
        new StringBuilder(
            " FROM "
                + prTableName
                + " AS "
                + prTableAlias
                + " INNER JOIN project_digitalobjects AS "
                + crossTableAlias
                + " ON "
                + prTableAlias
                + ".uuid = "
                + crossTableAlias
                + ".project_uuid"
                + " WHERE "
                + crossTableAlias
                + ".digitalobject_uuid = :uuid");
    Map<String, Object> argumentMappings = new HashMap<>(0);
    argumentMappings.put("uuid", digitalObjectUuid);
    String executedSearchTerm = addSearchTerm(pageRequest, commonSql, argumentMappings);
    Filtering filtering = pageRequest.getFiltering();
    // as filtering has other target object type (project) than this repository (digitalobject)
    // we have to rename filter field names to target table alias and column names:
    mapFilterExpressionsToOtherTableColumnNames(filtering, projectRepositoryImpl);
    addFiltering(pageRequest, commonSql, argumentMappings);

    StringBuilder innerQuery =
        new StringBuilder("SELECT " + crossTableAlias + ".sortindex AS idx, * " + commonSql);
    String orderBy = addCrossTablePageRequestParams(pageRequest, innerQuery, crossTableAlias);
    List<Project> result =
        projectRepositoryImpl.retrieveList(
            projectRepositoryImpl.getSqlSelectReducedFields(),
            innerQuery,
            argumentMappings,
            orderBy);

    StringBuilder countQuery = new StringBuilder("SELECT count(*)" + commonSql);
    long total = retrieveCount(countQuery, argumentMappings);

    return new PageResponse<>(result, pageRequest, total, executedSearchTerm);
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
  public String getColumnName(String modelProperty) {
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
  public List<Locale> getLanguagesOfContainedDigitalObjects(UUID uuid) {
    String sql =
        "SELECT DISTINCT jsonb_object_keys("
            + tableAlias
            + ".label)"
            + " FROM "
            + tableName
            + " AS "
            + tableAlias
            + String.format(" WHERE %s.parent_uuid = :uuid;", tableAlias);
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
  public DigitalObject save(DigitalObject digitalObject) {
    super.save(digitalObject);

    DigitalObject result = getByUuid(digitalObject.getUuid());
    return result;
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

  @Override
  public List<FileResource> setFileResources(
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

  public void setGeoLocationRepositoryImpl(
      GeoLocationRepositoryImpl<GeoLocation> geoLocationRepositoryImpl) {
    this.geoLocationRepositoryImpl = geoLocationRepositoryImpl;
  }

  public void setGeolocationEntityRepositoryImpl(
      EntityRepositoryImpl<GeoLocation> geolocationEntityRepositoryImpl) {
    this.geolocationEntityRepositoryImpl = geolocationEntityRepositoryImpl;
  }

  public void setLinkedDataFileResourceRepository(
      LinkedDataFileResourceRepositoryImpl linkedDataFileResourceRepositoryImpl) {
    this.linkedDataFileResourceRepositoryImpl = linkedDataFileResourceRepositoryImpl;
  }

  public void setPersonRepository(PersonRepositoryImpl personRepositoryImpl) {
    this.personRepositoryImpl = personRepositoryImpl;
  }

  @Override
  public DigitalObject update(DigitalObject digitalObject) {
    super.update(digitalObject);

    DigitalObject result = getByUuid(digitalObject.getUuid());
    return result;
  }
}
