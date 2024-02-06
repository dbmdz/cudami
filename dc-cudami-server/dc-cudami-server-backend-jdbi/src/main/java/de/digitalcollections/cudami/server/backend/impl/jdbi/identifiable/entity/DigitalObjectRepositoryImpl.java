package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.DigitalObjectRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent.AgentRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent.CorporateBodyRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent.PersonRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.geo.location.GeoLocationRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.geo.location.HumanSettlementRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.work.ItemRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource.DigitalObjectLinkedDataFileResourceRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource.DigitalObjectRenderingFileResourceRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource.FileResourceMetadataRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource.ImageFileResourceRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource.LinkedDataFileResourceRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.legal.LicenseRepositoryImpl;
import de.digitalcollections.cudami.server.config.BackendIiifServerConfig;
import de.digitalcollections.iiif.model.ImageContent;
import de.digitalcollections.iiif.model.jackson.IiifObjectMapper;
import de.digitalcollections.iiif.model.sharedcanvas.Canvas;
import de.digitalcollections.iiif.model.sharedcanvas.Manifest;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.IdentifiableObjectType;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.CustomAttributes;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.digitalobject.CreationInfo;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocation;
import de.digitalcollections.model.identifiable.entity.geo.location.HumanSettlement;
import de.digitalcollections.model.identifiable.entity.item.Item;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.identifiable.resource.LinkedDataFileResource;
import de.digitalcollections.model.legal.License;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.validation.ValidationException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;
import java.util.function.BiConsumer;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.generic.GenericType;
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

  @Lazy @Autowired private AgentRepositoryImpl<Agent> agentRepositoryImpl;
  @Lazy @Autowired private CollectionRepositoryImpl collectionRepositoryImpl;
  @Lazy @Autowired private CorporateBodyRepositoryImpl corporateBodyRepositoryImpl;

  @Lazy @Autowired
  private FileResourceMetadataRepositoryImpl<FileResource> fileResourceMetadataRepositoryImpl;

  @Lazy @Autowired private GeoLocationRepositoryImpl<GeoLocation> geoLocationRepositoryImpl;
  @Lazy @Autowired private HumanSettlementRepositoryImpl humanSettlementRepositoryImpl;
  @Lazy @Autowired private ImageFileResourceRepositoryImpl imageFileResourceRepositoryImpl;

  @Lazy @Autowired
  private LinkedDataFileResourceRepositoryImpl linkedDataFileResourceRepositoryImpl;

  @Lazy @Autowired private PersonRepositoryImpl personRepositoryImpl;
  @Lazy @Autowired private ProjectRepositoryImpl projectRepositoryImpl;

  private final IiifObjectMapper iiifObjectMapper;
  private final BackendIiifServerConfig iiifServerConfig;

  public DigitalObjectRepositoryImpl(
      Jdbi dbi,
      CudamiConfig cudamiConfig,
      BackendIiifServerConfig iiifServerConfig,
      IdentifierRepository identifierRepository,
      UrlAliasRepository urlAliasRepository,
      IiifObjectMapper iiifObjectMapper) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        DigitalObject.class,
        cudamiConfig.getOffsetForAlternativePaging(),
        identifierRepository,
        urlAliasRepository);
    this.iiifObjectMapper = iiifObjectMapper;
    this.iiifServerConfig = iiifServerConfig;
  }

  //
  // "canvases":[
  // {
  // "@id":
  // "https://api.digitale-sammlungen.de/iiif/presentation/v2/bsb00107608/canvas/1",
  // "@type": "sc:Canvas",
  // "label": "(0001)",
  // "images": [
  // {
  // "@type":"oa:Annotation",
  // "motivation": "sc:painting",
  // "resource": {
  // "@id":
  // "https://api.digitale-sammlungen.de/iiif/image/v2/bsb00107608_00001/full/full/0/default.jpg",
  // "@type": "dctypes:Image",
  // "service": {
  // "@context": "http://iiif.io/api/image/2/context.json",
  // "@id": "https://api.digitale-sammlungen.de/iiif/image/v2/bsb00107608_00001",
  // "profile": "http://iiif.io/api/image/2/level2.json",
  // "protocol": "http://iiif.io/api/image"
  // },
  // "format": "image/jpeg","width":6700,"height":4700}
  // ,"on":
  // "https://api.digitale-sammlungen.de/iiif/presentation/v2/bsb00107608/canvas/1"
  // }],"width":6700,"height":4700}, ...]
  //
  private ImageFileResource convertToImageFileResource(Canvas canvas) throws MalformedURLException {
    if (canvas.getImages() == null) {
      return null;
    }
    ImageContent imageContent = (ImageContent) canvas.getImages().get(0).getResource();
    de.digitalcollections.iiif.model.MimeType mimeType = imageContent.getFormat();
    URL httpBaseUrl = imageContent.getServices().get(0).getIdentifier().toURL();

    // TODO: add more image data as needed
    ImageFileResource ifr = new ImageFileResource();
    ifr.setHttpBaseUrl(httpBaseUrl);
    ifr.setMimeType(MimeType.fromTypename(mimeType.getTypeName()));
    return ifr;
  }

  @Override
  public DigitalObject create() throws RepositoryException {
    return new DigitalObject();
  }

  @Override
  protected void fullReduceRowsBiConsumer(Map<UUID, DigitalObject> map, RowView rowView) {
    super.fullReduceRowsBiConsumer(map, rowView);
    DigitalObject digitalObject = map.get(rowView.getColumn(MAPPING_PREFIX + "_uuid", UUID.class));

    // a small function that we need several times here but nowhere else
    BiConsumer<Identifiable, String> setIdentifiers =
        (identifiable, colName) -> {
          Set<Identifier> ids = rowView.getColumn(colName, new GenericType<Set<Identifier>>() {});
          if (ids != null) identifiable.setIdentifiers(ids);
        };

    // Try to fill license subresource with uuid, url and label
    License license = rowView.getRow(License.class);
    if (license.getUuid() != null) {
      digitalObject.setLicense(license);
    }

    // creator
    if (digitalObject.getCreationInfo() == null) {
      UUID creationCreatorUuid =
          rowView.getColumn(MAPPING_PREFIX + "_creation_creator_uuid", UUID.class);
      IdentifiableObjectType creatorType =
          rowView.getColumn("creator_objecttype", IdentifiableObjectType.class);
      LocalDate creationDate =
          rowView.getColumn(MAPPING_PREFIX + "_creation_date", LocalDate.class);
      UUID creationGeolocationUuid =
          rowView.getColumn(geoLocationRepositoryImpl.getMappingPrefix() + "_uuid", UUID.class);
      IdentifiableObjectType geoLocationType =
          rowView.getColumn(
              geoLocationRepositoryImpl.getMappingPrefix() + "_identifiableObjectType",
              IdentifiableObjectType.class);

      // If any of creation.creator.uuid, creation.geolocation.uuid or creation.date
      // is set,
      // We must build the CreationInfo object
      if (creationCreatorUuid != null || creationDate != null || creationGeolocationUuid != null) {
        CreationInfo creationInfo = new CreationInfo();
        if (creationCreatorUuid != null) {
          Agent creator =
              switch (creatorType) {
                case PERSON -> rowView.getRow(Person.class);
                case CORPORATE_BODY -> rowView.getRow(CorporateBody.class);
                default -> Agent.builder().uuid(creationCreatorUuid).build();
              };
          setIdentifiers.accept(creator, "creator_identifiers");
          creationInfo.setCreator(creator);
        }
        if (creationDate != null) {
          creationInfo.setDate(creationDate);
        }
        if (creationGeolocationUuid != null) {
          GeoLocation creationGeoLocation =
              switch (geoLocationType) {
                case HUMAN_SETTLEMENT -> rowView.getRow(HumanSettlement.class);
                default -> rowView.getRow(GeoLocation.class);
              };
          setIdentifiers.accept(creationGeoLocation, "creation_geolocation_identifiers");
          creationInfo.setGeoLocation(creationGeoLocation);
        }
        digitalObject.setCreationInfo(creationInfo);
      }
    }

    Integer numberOfBinaryResources =
        rowView.getColumn(MAPPING_PREFIX + "_number_binaryresources", Integer.class);
    if (numberOfBinaryResources != null) {
      digitalObject.setNumberOfBinaryResources(numberOfBinaryResources);
    }

    // parent
    UUID parentUuid = rowView.getColumn("parent_uuid", UUID.class);
    if (parentUuid != null
        && (digitalObject.getParent() == null || digitalObject.getParent().getCreated() == null)) {
      DigitalObject parent =
          DigitalObject.builder()
              .uuid(parentUuid)
              .label(rowView.getColumn("parent_label", LocalizedText.class))
              .description(
                  rowView.getColumn("parent_description", LocalizedStructuredContent.class))
              .customAttributes(
                  rowView.getColumn("parent_customAttributes", CustomAttributes.class))
              .refId(rowView.getColumn("parent_refId", Integer.class))
              .notes(
                  rowView.getColumn(
                      "parent_notes", new GenericType<List<LocalizedStructuredContent>>() {}))
              .created(rowView.getColumn("parent_created", LocalDateTime.class))
              .lastModified(rowView.getColumn("parent_lastModified", LocalDateTime.class))
              .build();
      setIdentifiers.accept(parent, "parent_identifiers");

      UUID parentsParentUuid = rowView.getColumn("parent_parentUuid", UUID.class);
      if (parentsParentUuid != null) {
        DigitalObject parentsParent = DigitalObject.builder().uuid(parentsParentUuid).build();
        setIdentifiers.accept(parentsParent, "parent_parentIdentifiers");
        parent.setParent(parentsParent);
      }

      UUID parentItemUuid = rowView.getColumn("parent_itemUuid", UUID.class);
      if (parentItemUuid != null) {
        Item parentsItem = Item.builder().uuid(parentItemUuid).build();
        setIdentifiers.accept(parentsItem, "parent_itemIdentifiers");
        parent.setItem(parentsItem);
      }

      digitalObject.setParent(parent);
    }

    // file resources
    UUID linkedDataFileResUuid =
        rowView.getColumn(
            linkedDataFileResourceRepositoryImpl.getMappingPrefix() + "_uuid", UUID.class);
    if (linkedDataFileResUuid != null) {
      if (digitalObject.getLinkedDataResources() == null
          || digitalObject.getLinkedDataResources().isEmpty()) {
        int maxIndex = rowView.getColumn("max_dold_sortindex", Integer.class);
        Vector<LinkedDataFileResource> resources = new Vector<>(++maxIndex);
        resources.setSize(maxIndex);
        digitalObject.setLinkedDataResources(resources);
      }
      LinkedDataFileResource ldFileResource = rowView.getRow(LinkedDataFileResource.class);
      if (!digitalObject.getLinkedDataResources().parallelStream()
          .anyMatch(res -> Objects.equals(res, ldFileResource))) {
        int idx = rowView.getColumn("dold_sortindex", Integer.class);
        digitalObject.getLinkedDataResources().set(idx, ldFileResource);
      }
    }

    // rendering resources
    UUID renderingResourceUuid =
        rowView.getColumn(
            fileResourceMetadataRepositoryImpl.getMappingPrefix() + "_uuid", UUID.class);
    if (renderingResourceUuid != null) {
      if (digitalObject.getRenderingResources() == null
          || digitalObject.getRenderingResources().isEmpty()) {
        int maxIndex = rowView.getColumn("max_dorr_sortindex", Integer.class);
        Vector<FileResource> resources = new Vector<>(++maxIndex);
        resources.setSize(maxIndex);
        digitalObject.setRenderingResources(resources);
      }
      FileResource renderingResource = rowView.getRow(FileResource.class);
      DigitalObjectRenderingFileResourceRepositoryImpl.fillResourceType(renderingResource);
      if (!digitalObject.getRenderingResources().parallelStream()
          .anyMatch(res -> Objects.equals(res, renderingResource))) {
        int idx = rowView.getColumn("dorr_sortindex", Integer.class);
        digitalObject.getRenderingResources().set(idx, renderingResource);
      }
    }
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
  protected void basicReduceRowsBiConsumer(Map<UUID, DigitalObject> map, RowView rowView) {
    super.basicReduceRowsBiConsumer(map, rowView);
    DigitalObject identifiable = map.get(rowView.getColumn(mappingPrefix + "_uuid", UUID.class));

    // Fill the parent (empty, only with uuid), if present.
    UUID parentUuid = rowView.getColumn(MAPPING_PREFIX + "_parent_uuid", UUID.class);
    if (parentUuid != null) {
      identifiable.setParent(DigitalObject.builder().uuid(parentUuid).build());
    }

    // set item UUID and label only
    UUID itemUuid = rowView.getColumn(MAPPING_PREFIX + "_item_uuid", UUID.class);
    LocalizedText itemLabel = rowView.getColumn("item_label", LocalizedText.class);
    Set<Identifier> itemIdentifiers =
        rowView.getColumn("item_identifiers", new GenericType<Set<Identifier>>() {});
    if (itemUuid != null) {
      identifiable.setItem(
          Item.builder().uuid(itemUuid).label(itemLabel).identifiers(itemIdentifiers).build());
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
    String orderBy =
        collectionRepositoryImpl.addCrossTablePagingAndSorting(
            pageRequest, innerQuery, crossTableAlias);
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
  public PageResponse<FileResource> findFileResources(
      UUID digitalObjectUuid, PageRequest pageRequest) throws RepositoryException {
    throw new UnsupportedOperationException(); // TODO: not yet implemented
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
    String orderBy =
        projectRepositoryImpl.addCrossTablePagingAndSorting(
            pageRequest, innerQuery, crossTableAlias);
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
  public PageResponse<DigitalObject> findDigitalObjectsByItem(
      UUID itemUuid, PageRequest pageRequest) throws RepositoryException {
    StringBuilder commonSql =
        new StringBuilder(
            " FROM "
                + tableName
                + " "
                + tableAlias
                + " WHERE "
                + tableAlias
                + ".item_uuid = :uuid");
    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("uuid", itemUuid);

    addFiltering(pageRequest, commonSql, argumentMappings);

    StringBuilder innerQuery = new StringBuilder("SELECT * " + commonSql);
    addPagingAndSorting(pageRequest, innerQuery);
    List<DigitalObject> result =
        retrieveList(
            getSqlSelectReducedFields(),
            innerQuery,
            argumentMappings,
            getOrderBy(pageRequest.getSorting()));

    StringBuilder countQuery = new StringBuilder("SELECT count(*)" + commonSql);
    long total = retrieveCount(countQuery, argumentMappings);

    return new PageResponse<>(result, pageRequest, total);
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

  /**
   * Until now it is using IIIF-Server as structure service. Maybe it is a database backend in the
   * future.
   */
  @Override
  public List<ImageFileResource> getIiifImageFileResources(UUID digitalObjectUuid)
      throws RepositoryException {
    DigitalObject digitalObject = getByUuid(digitalObjectUuid);

    URI iiifPresentationBaseUrl = iiifServerConfig.getPresentation().getBaseUrl();

    // default: iiif identifier = uuid
    String iiifIdentifier = digitalObjectUuid.toString();

    // custom: get iiif identifier from one of the identifiers of the digital object
    List<String> identifierNamespaces = iiifServerConfig.getIdentifier().getNamespaces();
    for (String identifierNamespace : identifierNamespaces) {
      Identifier identifier = digitalObject.getIdentifierByNamespace(identifierNamespace);
      if (identifier != null) {
        iiifIdentifier = identifier.getId();
        break;
      }
    }

    try {
      URL iiifManifestUrl = iiifPresentationBaseUrl.resolve(iiifIdentifier + "/manifest").toURL();
      Manifest manifest = iiifObjectMapper.readValue(iiifManifestUrl, Manifest.class);
      List<ImageFileResource> result = new ArrayList<>();
      List<Canvas> canvases = manifest.getDefaultSequence().getCanvases();
      for (Canvas canvas : canvases) {
        ImageFileResource ifr = convertToImageFileResource(canvas);
        if (ifr != null) {
          result.add(ifr);
        }
      }
      return result;
    } catch (MalformedURLException e) {
      throw new RepositoryException("can not create IIIF presentation URL for digital object", e);
    } catch (Exception e) {
      throw new RepositoryException("can not read IIIF presentation URL for digital object", e);
    }
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
  protected String getSqlInsertFields() {
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
  protected String getSqlInsertValues() {
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
    return super.getSqlSelectAllFields(tableAlias, mappingPrefix)
        + """
        , {{licenseTable}}.uuid {{licenseMapping}}_uuid,
        {{licenseTable}}.label {{licenseMapping}}_label,
        {{licenseTable}}.url {{licenseMapping}}_url,
        {{licenseTable}}.acronym {{licenseMapping}}_acronym,
        -- creators
        {{tableAlias}}.creation_creator_uuid {{mappingPrefix}}_creation_creator_uuid,
        get_identifiers({{tableAlias}}.creation_creator_uuid) creator_identifiers,
        {{agentAlias}}.identifiable_objecttype creator_objecttype,
        {{personFields}},
        {{corporationFields}},
        {{tableAlias}}.creation_date {{mappingPrefix}}_creation_date,
        {{tableAlias}}.creation_geolocation_uuid {{mappingPrefix}}_creation_geolocation_uuid,
        get_identifiers({{tableAlias}}.creation_geolocation_uuid) creation_geolocation_identifiers,
        {{geolocFields}},
        {{humanSettleFields}},
        -- binary resources
        {{tableAlias}}.number_binaryresources {{mappingPrefix}}_number_binaryresources,
        -- parent
        parent.uuid parent_uuid, get_identifiers(parent.uuid) parent_identifiers, parent.label parent_label,
        parent.refid parent_refId, parent.notes parent_notes, parent.created parent_created,
        parent.last_modified parent_lastModified, parent.parent_uuid parent_parentUuid, get_identifiers(parent.parent_uuid) parent_parentIdentifiers,
        parent.item_uuid parent_itemUuid, get_identifiers(parent.item_uuid) parent_itemIdentifiers,
        parent.custom_attrs parent_customAttributes, parent.description parent_description,
        -- linked data file resources
        {{digitalObjLinkedDataResAlias}}.sortindex dold_sortindex,
        max({{digitalObjLinkedDataResAlias}}.sortindex) over (partition by {{tableAlias}}.uuid) max_dold_sortindex,
        {{linkedDataFileResFields}},
        -- rendering resources
        {{digitalObjRenderingResAlias}}.sortindex dorr_sortindex,
        max({{digitalObjRenderingResAlias}}.sortindex) over (partition by {{tableAlias}}.uuid) max_dorr_sortindex,
        {{renderingResFields}}
        """
            .replace("{{tableAlias}}", tableAlias)
            .replace("{{mappingPrefix}}", mappingPrefix)
            .replace("{{licenseTable}}", LicenseRepositoryImpl.TABLE_ALIAS)
            .replace("{{licenseMapping}}", LicenseRepositoryImpl.MAPPING_PREFIX)
            // creation info: creator
            .replace("{{agentAlias}}", agentRepositoryImpl.getTableAlias())
            .replace("{{personFields}}", personRepositoryImpl.getSqlSelectReducedFields())
            .replace("{{corporationFields}}", corporateBodyRepositoryImpl.getSqlSelectAllFields())
            // creation info: geo location
            .replace("{{geolocFields}}", geoLocationRepositoryImpl.getSqlSelectAllFields())
            .replace("{{humanSettleFields}}", humanSettlementRepositoryImpl.getSqlSelectAllFields())
            // linked data
            .replace(
                "{{digitalObjLinkedDataResAlias}}",
                DigitalObjectLinkedDataFileResourceRepositoryImpl.TABLE_ALIAS)
            .replace(
                "{{linkedDataFileResFields}}",
                linkedDataFileResourceRepositoryImpl.getSqlSelectAllFields())
            // rendering resources
            .replace(
                "{{digitalObjRenderingResAlias}}",
                DigitalObjectRenderingFileResourceRepositoryImpl.TABLE_ALIAS)
            .replace(
                "{{renderingResFields}}",
                fileResourceMetadataRepositoryImpl.getSqlSelectReducedFields());
  }

  @Override
  protected String getSqlSelectAllFieldsJoins() {
    return super.getSqlSelectAllFieldsJoins()
        + """
        LEFT JOIN {{licenseTable}} {{licenseAlias}}
          ON {{tableAlias}}.license_uuid = {{licenseAlias}}.uuid
        -- creation info creator
        LEFT JOIN {{agentTable}} {{agentAlias}}
          ON {{tableAlias}}.creation_creator_uuid = {{agentAlias}}.uuid
        LEFT JOIN {{corporationTable}} {{corporationAlias}}
          ON {{agentAlias}}.uuid = {{corporationAlias}}.uuid
        LEFT JOIN {{personTable}} {{personAlias}}
          ON {{agentAlias}}.uuid = {{personAlias}}.uuid
        -- creation info geolocation
        LEFT JOIN {{geolocTable}} {{geolocAlias}}
          ON {{tableAlias}}.creation_geolocation_uuid = {{geolocAlias}}.uuid
        LEFT JOIN {{humanSettleTable}} {{humanSettleAlias}}
          ON {{geolocAlias}}.uuid = {{humanSettleAlias}}.uuid
        -- parent
        LEFT JOIN {{tableName}} parent
          ON {{tableAlias}}.parent_uuid = parent.uuid
        LEFT JOIN (
          {{digitalObjLinkedDataResTable}} {{digitalObjLinkedDataResAlias}} INNER JOIN {{linkedDataFileResTable}} {{linkedDataFileResAlias}}
          ON {{digitalObjLinkedDataResAlias}}.linkeddata_fileresource_uuid = {{linkedDataFileResAlias}}.uuid
        ) ON {{digitalObjLinkedDataResAlias}}.digitalobject_uuid = {{tableAlias}}.uuid
        LEFT JOIN (
          {{digitalObjRenderingResTable}} {{digitalObjRenderingResAlias}} INNER JOIN {{renderingResourcesTable}} {{renderingResourcesAlias}}
          ON {{digitalObjRenderingResAlias}}.fileresource_uuid = {{renderingResourcesAlias}}.uuid
        ) ON {{digitalObjRenderingResAlias}}.digitalobject_uuid = {{tableAlias}}.uuid
        """
            .replace("{{tableName}}", tableName)
            .replace("{{tableAlias}}", tableAlias)
            // license
            .replace("{{licenseTable}}", LicenseRepositoryImpl.TABLE_NAME)
            .replace("{{licenseAlias}}", LicenseRepositoryImpl.TABLE_ALIAS)
            // creation info: creator
            .replace("{{agentTable}}", agentRepositoryImpl.getTableName())
            .replace("{{agentAlias}}", agentRepositoryImpl.getTableAlias())
            .replace("{{corporationTable}}", corporateBodyRepositoryImpl.getTableName())
            .replace("{{corporationAlias}}", corporateBodyRepositoryImpl.getTableAlias())
            .replace("{{personTable}}", personRepositoryImpl.getTableName())
            .replace("{{personAlias}}", personRepositoryImpl.getTableAlias())
            // creation info: geo location
            .replace("{{geolocTable}}", geoLocationRepositoryImpl.getTableName())
            .replace("{{geolocAlias}}", geoLocationRepositoryImpl.getTableAlias())
            .replace("{{humanSettleTable}}", HumanSettlementRepositoryImpl.TABLE_NAME)
            .replace("{{humanSettleAlias}}", HumanSettlementRepositoryImpl.TABLE_ALIAS)
            // linked data file resources
            .replace(
                "{{digitalObjLinkedDataResTable}}",
                DigitalObjectLinkedDataFileResourceRepositoryImpl.TABLE_NAME)
            .replace(
                "{{digitalObjLinkedDataResAlias}}",
                DigitalObjectLinkedDataFileResourceRepositoryImpl.TABLE_ALIAS)
            .replace(
                "{{linkedDataFileResTable}}", linkedDataFileResourceRepositoryImpl.getTableName())
            .replace(
                "{{linkedDataFileResAlias}}", linkedDataFileResourceRepositoryImpl.getTableAlias())
            // rendering resources
            .replace(
                "{{digitalObjRenderingResTable}}",
                DigitalObjectRenderingFileResourceRepositoryImpl.TABLE_NAME)
            .replace(
                "{{digitalObjRenderingResAlias}}",
                DigitalObjectRenderingFileResourceRepositoryImpl.TABLE_ALIAS)
            .replace(
                "{{renderingResourcesTable}}", fileResourceMetadataRepositoryImpl.getTableName())
            .replace(
                "{{renderingResourcesAlias}}", fileResourceMetadataRepositoryImpl.getTableAlias());
  }

  @Override
  public String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return super.getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + """
        , %1$s.parent_uuid %2$s_parent_uuid,
        %1$s.item_uuid %2$s_item_uuid, get_identifiers(%1$s.item_uuid) item_identifiers,
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
  protected String getSqlUpdateFieldValues() {
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
  public List<FileResource> setFileResources(
      UUID digitalObjectUuid, List<FileResource> fileResources)
      throws RepositoryException, ValidationException {

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

  // --------- repository setters for testing purposes only ----------------------
  public void setAgentRepository(AgentRepositoryImpl<Agent> agentRepositoryImpl) {
    this.agentRepositoryImpl = agentRepositoryImpl;
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

  public void setGeoLocationRepositoryImpl(
      GeoLocationRepositoryImpl<GeoLocation> geoLocationRepositoryImpl) {
    this.geoLocationRepositoryImpl = geoLocationRepositoryImpl;
  }

  public void setHumanSettlementRepository(HumanSettlementRepositoryImpl repositoryImpl) {
    this.humanSettlementRepositoryImpl = repositoryImpl;
  }

  public void setLinkedDataFileResourceRepository(
      LinkedDataFileResourceRepositoryImpl linkedDataFileResourceRepositoryImpl) {
    this.linkedDataFileResourceRepositoryImpl = linkedDataFileResourceRepositoryImpl;
  }

  public void setPersonRepository(PersonRepositoryImpl personRepositoryImpl) {
    this.personRepositoryImpl = personRepositoryImpl;
  }
}
