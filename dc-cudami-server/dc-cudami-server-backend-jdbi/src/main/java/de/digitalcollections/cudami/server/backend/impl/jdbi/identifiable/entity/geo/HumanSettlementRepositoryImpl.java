package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.geo;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.geo.HumanSettlementRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.geo.HumanSettlement;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.IdentifierImpl;
import de.digitalcollections.model.impl.identifiable.entity.geo.HumanSettlementImpl;
import de.digitalcollections.model.impl.identifiable.resource.ImageFileResourceImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class HumanSettlementRepositoryImpl extends IdentifiableRepositoryImpl<HumanSettlement>
    implements HumanSettlementRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(HumanSettlementRepositoryImpl.class);

  @Autowired
  public HumanSettlementRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository) {
    super(dbi, identifierRepository);
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM humansettlements";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOne().get());
    return count;
  }

  @Override
  public PageResponse<HumanSettlement> find(PageRequest pageRequest) {
    // TODO make dependend from language the user has chosen...
    String language = "de";

    // select only what is shown/needed in alphabetical sorted paged list:
    StringBuilder query =
        new StringBuilder(
            "SELECT hs.uuid hs_uuid, hs.label hs_label, hs.description hs_description,"
                //      + " hs.identifiable_type hs_type, hs.entity_type hs_entityType,
                // hs.geolocation_type hs_geoLocationType,"
                //      + " hs.created hs_created, hs.last_modified hs_last_modified,"
                //      + " hs.coordinate_location hs_coordinateLocation, hs.settlement_type
                // hs_humanSettlementType,"
                //      + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace
                // id_namespace, id.identifier id_id,"
                + " file.uri f_uri, file.filename f_filename"
                + " FROM humansettlements as hs"
                //      + " LEFT JOIN identifiers as id on hs.uuid = id.identifiable"
                + " LEFT JOIN fileresources_image as file on hs.previewfileresource = file.uuid"
                + " ORDER BY hs.label ->> :language");
    addPageRequestParams(pageRequest, query);

    List<HumanSettlement> result =
        new ArrayList(
            dbi.withHandle(
                h ->
                    h.createQuery(query.toString())
                        .bind("language", language)
                        .registerRowMapper(BeanMapper.factory(HumanSettlementImpl.class, "hs"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, HumanSettlement>(),
                            (map, rowView) -> {
                              HumanSettlement hs =
                                  map.computeIfAbsent(
                                      rowView.getColumn("hs_uuid", UUID.class),
                                      uuid -> rowView.getRow(HumanSettlementImpl.class));
                              if (rowView.getColumn("f_uri", String.class) != null) {
                                hs.setPreviewImage(rowView.getRow(ImageFileResourceImpl.class));
                              }
                              return map;
                            })
                        .values()));

    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public PageResponse<HumanSettlement> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial) {
    // select only what is shown/needed in alphabetical sorted paged list:
    StringBuilder query =
        new StringBuilder(
            "SELECT hs.uuid hs_uuid, hs.label hs_label, hs.description hs_description,"
                //      + " hs.identifiable_type hs_type, hs.entity_type hs_entityType,
                // hs.geolocation_type hs_geoLocationType,"
                //      + " hs.created hs_created, hs.last_modified hs_last_modified,"
                //      + " hs.coordinate_location hs_coordinateLocation, hs.settlement_type
                // hs_humanSettlementType,"
                //      + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace
                // id_namespace, id.identifier id_id,"
                + " file.uri f_uri, file.filename f_filename"
                + " FROM humansettlements as hs"
                //      + " LEFT JOIN identifiers as id on hs.uuid = id.identifiable"
                + " LEFT JOIN fileresources_image as file on hs.previewfileresource = file.uuid"
                + " WHERE hs.label ->> :language IS NOT null AND hs.label ->> :language ILIKE :initial || '%'"
                + " ORDER BY hs.label ->> :language");
    addPageRequestParams(pageRequest, query);

    List<HumanSettlement> result =
        new ArrayList(
            dbi.withHandle(
                h ->
                    h.createQuery(query.toString())
                        .bind("language", language)
                        .bind("initial", initial)
                        .registerRowMapper(BeanMapper.factory(HumanSettlementImpl.class, "hs"))
                        .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                        .reduceRows(
                            new LinkedHashMap<UUID, HumanSettlement>(),
                            (map, rowView) -> {
                              HumanSettlement hs =
                                  map.computeIfAbsent(
                                      rowView.getColumn("hs_uuid", UUID.class),
                                      uuid -> rowView.getRow(HumanSettlementImpl.class));
                              if (rowView.getColumn("f_uri", String.class) != null) {
                                hs.setPreviewImage(rowView.getRow(ImageFileResourceImpl.class));
                              }
                              return map;
                            })
                        .values()));

    String countQuery =
        "SELECT count(*) FROM humansettlements as hs"
            + " WHERE hs.label ->> :language IS NOT null AND hs.label ->> :language ILIKE :initial || '%'";
    long total =
        dbi.withHandle(
            h ->
                h.createQuery(countQuery)
                    .bind("language", language)
                    .bind("initial", initial)
                    .mapTo(Long.class)
                    .findOne()
                    .get());

    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public HumanSettlement findOne(UUID uuid) {
    String query =
        "SELECT hs.uuid hs_uuid, hs.label hs_label, hs.description hs_description,"
            + " hs.identifiable_type hs_type, hs.entity_type hs_entityType, hs.geolocation_type hs_geoLocationType,"
            + " hs.created hs_created, hs.last_modified hs_last_modified,"
            + " hs.coordinate_location hs_coordinateLocation, hs.settlement_type hs_humanSettlementType,"
            + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
            + " file.uri f_uri, file.filename f_filename"
            + " FROM humansettlements as hs"
            + " LEFT JOIN identifiers as id on hs.uuid = id.identifiable"
            + " LEFT JOIN fileresources_image as file on hs.previewfileresource = file.uuid"
            + " WHERE hs.uuid = :uuid";
    Optional<HumanSettlement> resultOpt =
        dbi.withHandle(
            h ->
                h
                    .createQuery(query)
                    .bind("uuid", uuid)
                    .registerRowMapper(BeanMapper.factory(HumanSettlementImpl.class, "hs"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                    .reduceRows(
                        new LinkedHashMap<UUID, HumanSettlement>(),
                        (map, rowView) -> {
                          HumanSettlement hs =
                              map.computeIfAbsent(
                                  rowView.getColumn("hs_uuid", UUID.class),
                                  id -> rowView.getRow(HumanSettlementImpl.class));
                          if (rowView.getColumn("id_uuid", UUID.class) != null) {
                            hs.addIdentifier(rowView.getRow(IdentifierImpl.class));
                          }
                          if (rowView.getColumn("f_uri", String.class) != null) {
                            hs.setPreviewImage(rowView.getRow(ImageFileResourceImpl.class));
                          }
                          return map;
                        })
                    .values()
                    .stream()
                    .findFirst());
    if (!resultOpt.isPresent()) {
      return null;
    }
    return resultOpt.get();
  }

  @Override
  public HumanSettlement findOne(Identifier identifier) {
    if (identifier.getIdentifiable() != null) {
      return findOne(identifier.getIdentifiable());
    }

    String namespace = identifier.getNamespace();
    String identifierId = identifier.getId();

    String query =
        "SELECT hs.uuid hs_uuid, hs.label hs_label, hs.description hs_description,"
            + " hs.identifiable_type hs_type, hs.entity_type hs_entityType, hs.geolocation_type hs_geoLocationType,"
            + " hs.created hs_created, hs.last_modified hs_last_modified,"
            + " hs.coordinate_location hs_coordinateLocation, hs.settlement_type hs_humanSettlementType,"
            + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
            + " file.uri f_uri, file.filename f_filename"
            + " FROM humansettlements as hs"
            + " LEFT JOIN identifiers as id on hs.uuid = id.identifiable"
            + " LEFT JOIN fileresources_image as file on hs.previewfileresource = file.uuid"
            + " WHERE id.identifier = :id AND id.namespace = :namespace";

    Optional<HumanSettlement> resultOpt =
        dbi.withHandle(
            h ->
                h
                    .createQuery(query)
                    .bind("id", identifierId)
                    .bind("namespace", namespace)
                    .registerRowMapper(BeanMapper.factory(HumanSettlementImpl.class, "hs"))
                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "f"))
                    .reduceRows(
                        new LinkedHashMap<UUID, HumanSettlement>(),
                        (map, rowView) -> {
                          HumanSettlement hs =
                              map.computeIfAbsent(
                                  rowView.getColumn("hs_uuid", UUID.class),
                                  id -> rowView.getRow(HumanSettlementImpl.class));
                          if (rowView.getColumn("id_uuid", UUID.class) != null) {
                            hs.addIdentifier(rowView.getRow(IdentifierImpl.class));
                          }
                          if (rowView.getColumn("f_uri", String.class) != null) {
                            hs.setPreviewImage(rowView.getRow(ImageFileResourceImpl.class));
                          }
                          return map;
                        })
                    .values()
                    .stream()
                    .findFirst());
    if (!resultOpt.isPresent()) {
      return null;
    }
    return resultOpt.get();
  }

  @Override
  public HumanSettlement findOneByIdentifier(String namespace, String id) {
    return findOne(new IdentifierImpl(null, namespace, id));
  }

  @Override
  public HumanSettlement save(HumanSettlement humanSettlement) {
    if (humanSettlement.getUuid() == null) {
      humanSettlement.setUuid(UUID.randomUUID());
    }
    humanSettlement.setCreated(LocalDateTime.now());
    humanSettlement.setLastModified(LocalDateTime.now());

    final UUID previewImageUuid =
        humanSettlement.getPreviewImage() == null
            ? null
            : humanSettlement.getPreviewImage().getUuid();

    String query =
        "INSERT INTO humansettlements("
            + "uuid, previewFileResource, label, description,"
            + " identifiable_type, entity_type, geolocation_type,"
            + " created, last_modified,"
            + " coordinate_location, settlement_type"
            + ") VALUES ("
            + ":uuid, :previewFileResource, :label::JSONB, :description::JSONB,"
            + " :type, :entityType, :geoLocationType,"
            + " :created, :lastModified,"
            + " :coordinateLocation::JSONB, :humanSettlementType"
            + ")";
    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(humanSettlement)
                .execute());

    // save identifiers
    Set<Identifier> identifiers = humanSettlement.getIdentifiers();
    saveIdentifiers(identifiers, humanSettlement);

    HumanSettlement result = findOne(humanSettlement.getUuid());
    return result;
  }

  @Override
  public HumanSettlement update(HumanSettlement humanSettlement) {
    // uuid and created stay unchanged, update last modified
    humanSettlement.setLastModified(LocalDateTime.now());
    final UUID previewImageUuid =
        humanSettlement.getPreviewImage() == null
            ? null
            : humanSettlement.getPreviewImage().getUuid();

    String query =
        "UPDATE humansettlements SET"
            + " previewFileResource=:previewFileResource, label=:label::JSONB, description=:description::JSONB,"
            + " geolocation_type=:geoLocationType,"
            + " last_modified=:lastModified,"
            + " coordinate_location=:coordinateLocation::JSONB, settlement_type=:humanSettlementType"
            + " WHERE uuid=:uuid";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(humanSettlement)
                .execute());

    // save identifiers
    Set<Identifier> identifiers = humanSettlement.getIdentifiers();
    // as we store the whole list new: delete old entries
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM identifiers WHERE identifiable = :uuid")
                .bind("uuid", humanSettlement.getUuid())
                .execute());
    saveIdentifiers(identifiers, humanSettlement);

    HumanSettlement result = findOne(humanSettlement.getUuid());
    return result;
  }
}
