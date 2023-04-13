package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.DigitalObjectRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent.CorporateBodyRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent.PersonRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.geo.location.GeoLocationRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.work.ItemRepositoryImpl;
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
import de.digitalcollections.model.text.LocalizedText;
import java.time.LocalDate;
import java.util.*;
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
  public static final String TABLE_NAME = "digitalobjects";

  @Lazy @Autowired private EntityRepositoryImpl<Agent> agentEntityRepositoryImpl;

  @Lazy @Autowired private CollectionRepositoryImpl collectionRepositoryImpl;

  @Lazy @Autowired private CorporateBodyRepositoryImpl corporateBodyRepositoryImpl;

  @Lazy @Autowired
  private FileResourceMetadataRepositoryImpl<FileResource> fileResourceMetadataRepositoryImpl;

  @Lazy @Autowired private EntityRepositoryImpl<GeoLocation> geolocationEntityRepositoryImpl;

  @Lazy @Autowired private GeoLocationRepositoryImpl<GeoLocation> geoLocationRepositoryImpl;

  @Lazy @Autowired private ImageFileResourceRepositoryImpl imageFileResourceRepositoryImpl;

  @Lazy @Autowired
  private LinkedDataFileResourceRepositoryImpl linkedDataFileResourceRepositoryImpl;

  @Lazy @Autowired private PersonRepositoryImpl personRepositoryImpl;

  @Lazy @Autowired private ProjectRepositoryImpl projectRepositoryImpl;

  public DigitalObjectRepositoryImpl(
      Jdbi dbi,
      CudamiConfig cudamiConfig,
      IdentifierRepository identifierRepository,
      UrlAliasRepository urlAliasRepository) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        DigitalObject.class,
        cudamiConfig.getOffsetForAlternativePaging(),
        identifierRepository,
        urlAliasRepository);
  }

  @Override
  public DigitalObject create() throws RepositoryException {
    return new DigitalObject();
  }

  @Override
  protected BiConsumer<Map<UUID, DigitalObject>, RowView> createAdditionalReduceRowsBiConsumer() {
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

      // If any of creation.creator.uuid, creation.geolocation.uuid or creation.date
      // is set,
      // We must build the CreationInfo object
      if (creationCreatorUuid != null || creationDate != null || creationGeolocationUuid != null) {
        CreationInfo creationInfo = new CreationInfo();
        if (creationCreatorUuid != null) {
          creationInfo.setCreator(Agent.builder().uuid(creationCreatorUuid).build());
        }
        if (creationDate != null) {
          creationInfo.setDate(creationDate);
        }
        if (creationGeolocationUuid != null) {
          creationInfo.setGeoLocation(GeoLocation.builder().uuid(creationGeolocationUuid).build());
        }
        digitalObject.setCreationInfo(creationInfo);
      }

      Integer numberOfBinaryResources =
          rowView.getColumn(MAPPING_PREFIX + "_number_binaryresources", Integer.class);
      if (numberOfBinaryResources != null) {
        digitalObject.setNumberOfBinaryResources(numberOfBinaryResources);
      }
    };
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

    // set item UUID and label only
    UUID itemUuid = rowView.getColumn(MAPPING_PREFIX + "_item_uuid", UUID.class);
    LocalizedText itemLabel = rowView.getColumn("item_label", LocalizedText.class);
    if (itemUuid != null) {
      identifiable.setItem(Item.builder().uuid(itemUuid).label(itemLabel).build());
    }
  }

  private void fillAttributes(DigitalObject digitalObject) throws RepositoryException {
    if (digitalObject == null) {
      return;
    }

    // Fill the previewImage
    UUID previewImageUuid =
        digitalObject.getPreviewImage() != null ? digitalObject.getPreviewImage().getUuid() : null;
    if (previewImageUuid != null) {
      digitalObject.setPreviewImage(imageFileResourceRepositoryImpl.getByUuid(previewImageUuid));
    }

    // If CreationInfo is set, retrieve the UUIDs of agent and place and fill their
    // objects
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
  public PageResponse<Collection> findCollections(UUID digitalObjectUuid, PageRequest pageRequest)
      throws RepositoryException {
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
    Filtering filtering = pageRequest.getFiltering();
    // as filtering has other target object type (collection) than this repository
    // (digitalobject)
    // we have to rename filter field names to target table alias and column names:
    mapFilterExpressionsToOtherTableColumnNames(filtering, collectionRepositoryImpl);
    addFiltering(pageRequest, commonSql, argumentMappings);

    StringBuilder innerQuery =
        new StringBuilder("SELECT " + crossTableAlias + ".sortindex AS idx, * " + commonSql);
    String orderBy = addCrossTablePagingAndSorting(pageRequest, innerQuery, crossTableAlias);
    List<Collection> result =
        collectionRepositoryImpl.retrieveList(
            collectionRepositoryImpl.getSqlSelectReducedFields(),
            innerQuery,
            argumentMappings,
            orderBy);

    StringBuilder countQuery = new StringBuilder("SELECT count(*)" + commonSql);
    long total = retrieveCount(countQuery, argumentMappings);

    return new PageResponse<>(result, pageRequest, total);
  }

  @Override
  public List<FileResource> getFileResources(UUID digitalObjectUuid) throws RepositoryException {
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
  public PageResponse<FileResource> findFileResources(
      UUID digitalObjectUuid, PageRequest pageRequest) throws RepositoryException {
    throw new UnsupportedOperationException(); // TODO: not yet implemented
  }

  @Override
  public List<ImageFileResource> getImageFileResources(UUID digitalObjectUuid)
      throws RepositoryException {
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
  public PageResponse<ImageFileResource> findImageFileResources(
      UUID digitalObjectUuid, PageRequest pageRequest) throws RepositoryException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PageResponse<Project> findProjects(UUID digitalObjectUuid, PageRequest pageRequest)
      throws RepositoryException {
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
    Filtering filtering = pageRequest.getFiltering();
    // as filtering has other target object type (project) than this repository
    // (digitalobject)
    // we have to rename filter field names to target table alias and column names:
    mapFilterExpressionsToOtherTableColumnNames(filtering, projectRepositoryImpl);
    addFiltering(pageRequest, commonSql, argumentMappings);

    StringBuilder innerQuery =
        new StringBuilder("SELECT " + crossTableAlias + ".sortindex AS idx, * " + commonSql);
    String orderBy = addCrossTablePagingAndSorting(pageRequest, innerQuery, crossTableAlias);
    List<Project> result =
        projectRepositoryImpl.retrieveList(
            projectRepositoryImpl.getSqlSelectReducedFields(),
            innerQuery,
            argumentMappings,
            orderBy);

    StringBuilder countQuery = new StringBuilder("SELECT count(*)" + commonSql);
    long total = retrieveCount(countQuery, argumentMappings);

    return new PageResponse<>(result, pageRequest, total);
  }

  @Override
  /**
   * Returns the fully filled DigitalObject including all of its direct attributes.
   *
   * <p>If a belonging item exists for this DigitalObject, the Item is returned with nothing but its
   * UUID set, and the client is responsible for retrieving the whole item object!
   */
  public DigitalObject getByIdentifier(Identifier identifier) throws RepositoryException {
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
  public DigitalObject getByUuidAndFiltering(UUID uuid, Filtering filtering)
      throws RepositoryException {
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
    // return null;
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
  protected String getSqlSelectAllFieldsJoins() {
    return super.getSqlSelectAllFieldsJoins()
        + " LEFT JOIN "
        + LicenseRepositoryImpl.TABLE_NAME
        + " AS "
        + LicenseRepositoryImpl.TABLE_ALIAS
        + " ON "
        + TABLE_ALIAS
        + ".license_uuid = "
        + LicenseRepositoryImpl.TABLE_ALIAS
        + ".uuid";
  }

  @Override
  public String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return super.getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + """
        , %1$s.parent_uuid %2$s_parent_uuid,
        %1$s.item_uuid %2$s_item_uuid,
        %3$s.label item_label"""
            .formatted(tableAlias, mappingPrefix, ItemRepositoryImpl.TABLE_ALIAS);
  }

  @Override
  protected String getSqlSelectReducedFieldsJoins() {
    return super.getSqlSelectReducedFieldsJoins()
        + " LEFT JOIN %1$s %2$s ON %2$s.uuid = %3$s.item_uuid"
            .formatted(ItemRepositoryImpl.TABLE_NAME, ItemRepositoryImpl.TABLE_ALIAS, TABLE_ALIAS);
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

  @Override
  public void save(DigitalObject digitalObject) throws RepositoryException {
    super.save(digitalObject);
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
      UUID digitalObjectUuid, List<FileResource> fileResources) throws RepositoryException {

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
          try {
            fileResourceMetadataRepositoryImpl.save(fileResource);
          } catch (RepositoryException e) {
            throw new RepositoryException("File resource cannot be saved properly!", e);
          }
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

  public void setGeolocationEntityRepositoryImpl(
      EntityRepositoryImpl<GeoLocation> geolocationEntityRepositoryImpl) {
    this.geolocationEntityRepositoryImpl = geolocationEntityRepositoryImpl;
  }

  public void setGeoLocationRepositoryImpl(
      GeoLocationRepositoryImpl<GeoLocation> geoLocationRepositoryImpl) {
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
