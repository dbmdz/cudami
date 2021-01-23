package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent.PersonRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.DigitalObjectRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.EntityRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.work.WorkRepositoryImpl;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.entity.agent.Person;
import de.digitalcollections.model.api.identifiable.entity.geo.GeoLocation;
import de.digitalcollections.model.api.identifiable.entity.geo.enums.GeoLocationType;
import de.digitalcollections.model.api.identifiable.entity.work.Work;
import de.digitalcollections.model.api.identifiable.parts.LocalizedText;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.agent.PersonImpl;
import de.digitalcollections.model.impl.identifiable.entity.geo.GeoLocationImpl;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.result.RowView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class PersonRepositoryImpl extends EntityRepositoryImpl<Person> implements PersonRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(PersonRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "pe";
  public static final String TABLE_ALIAS = "p";
  private static final String SQL_FULL_FIELDS_JOINS =
      " LEFT JOIN geolocations AS glbirth ON glbirth.uuid = "
          + TABLE_ALIAS
          + ".locationofbirth"
          + " LEFT JOIN geolocations AS gldeath ON gldeath.uuid = "
          + TABLE_ALIAS
          + ".locationofdeath";
  public static final String TABLE_NAME = "persons";

  private static BiFunction<LinkedHashMap<UUID, Person>, RowView, LinkedHashMap<UUID, Person>>
      createAdditionalReduceRowsBiFunction() {
    return (map, rowView) -> {
      // entity should be already in map, as we here just add additional data
      Person person = map.get(rowView.getColumn(MAPPING_PREFIX + "_uuid", UUID.class));

      if (rowView.getColumn("glbirth_uuid", UUID.class) != null) {
        UUID glBirthUuid = rowView.getColumn("glbirth_uuid", UUID.class);
        LocalizedText label = rowView.getColumn("glbirth_label", LocalizedText.class);
        GeoLocationType geoLocationType =
            rowView.getColumn("glbirth_geoLocationType", GeoLocationType.class);
        final GeoLocation placeOfBirth = new GeoLocationImpl();
        placeOfBirth.setUuid(glBirthUuid);
        placeOfBirth.setLabel(label);
        placeOfBirth.setGeoLocationType(geoLocationType);
        person.setPlaceOfBirth(placeOfBirth);
      }

      if (rowView.getColumn("gldeath_uuid", UUID.class) != null) {
        UUID glDeathUuid = rowView.getColumn("gldeath_uuid", UUID.class);
        LocalizedText label = rowView.getColumn("gldeath_label", LocalizedText.class);
        GeoLocationType geoLocationType =
            rowView.getColumn("gldeath_geoLocationType", GeoLocationType.class);
        final GeoLocation placeOfDeath = new GeoLocationImpl();
        placeOfDeath.setUuid(glDeathUuid);
        placeOfDeath.setLabel(label);
        placeOfDeath.setGeoLocationType(geoLocationType);
        person.setPlaceOfDeath(placeOfDeath);
      }

      return map;
    };
  }

  public static String getSqlAllFields(String tableAlias, String mappingPrefix) {
    return getSqlReducedFields(tableAlias, mappingPrefix)
        + ", "
        + "glbirth.uuid glbirth_uuid, glbirth.label glbirth_label, glbirth.geolocation_type glbirth_geoLocationType, "
        + "gldeath.uuid gldeath_uuid, gldeath.label gldeath_label, gldeath.geolocation_type gldeath_geoLocationType";
  }

  public static String getSqlReducedFields(String tableAlias, String mappingPrefix) {
    return EntityRepositoryImpl.getSqlReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".date_published "
        + mappingPrefix
        + "_datePublished, "
        + tableAlias
        + ".dateofbirth "
        + mappingPrefix
        + "_dateOfBirth, "
        + tableAlias
        + ".dateofdeath "
        + mappingPrefix
        + "_dateOfDeath, "
        + tableAlias
        + ".gender "
        + mappingPrefix
        + "_gender, "
        + tableAlias
        + ".timevalue_published "
        + mappingPrefix
        + "_timeValuePublished, "
        + tableAlias
        + ".timevalueofbirth "
        + mappingPrefix
        + "_timeValueOfBirth, "
        + tableAlias
        + ".timevalueofdeath "
        + mappingPrefix
        + "_timeValueOfDeath";
  }

  private final DigitalObjectRepositoryImpl digitalObjectRepositoryImpl;
  private final WorkRepositoryImpl workRepositoryImpl;

  @Autowired
  public PersonRepositoryImpl(
      Jdbi dbi,
      IdentifierRepository identifierRepository,
      DigitalObjectRepositoryImpl digitalObjectRepositoryImpl,
      WorkRepositoryImpl workRepositoryImpl) {
    super(
        dbi,
        identifierRepository,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        PersonImpl.class,
        SQL_FULL_FIELDS_JOINS,
        createAdditionalReduceRowsBiFunction());
    this.digitalObjectRepositoryImpl = digitalObjectRepositoryImpl;
    this.workRepositoryImpl = workRepositoryImpl;
  }

  @Override
  public PageResponse<Person> findByLocationOfBirth(PageRequest pageRequest, UUID uuidGeoLocation) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public PageResponse<Person> findByLocationOfDeath(PageRequest pageRequest, UUID uuidGeoLocation) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Set<DigitalObject> getDigitalObjects(UUID uuidPerson) {
    final String doTableAlias = digitalObjectRepositoryImpl.getTableAlias();
    final String doTableName = digitalObjectRepositoryImpl.getTableName();

    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT * FROM "
                + doTableName
                + " AS "
                + doTableAlias
                + " LEFT JOIN item_digitalobjects AS itdi ON "
                + doTableAlias
                + ".uuid = itdi.digitalobject_uuid"
                + " LEFT JOIN item_works AS itwo ON itdi.item_uuid = itwo.item_uuid"
                + " LEFT JOIN work_creators AS wocr ON itwo.work_uuid = wocr.work_uuid"
                + " WHERE wocr.agent_uuid = :uuid");

    List<DigitalObject> list =
        digitalObjectRepositoryImpl.retrieveList(
            digitalObjectRepositoryImpl.getSqlReducedFields(),
            innerQuery,
            Map.of("uuid", uuidPerson));

    return list.stream().collect(Collectors.toSet());
  }

  @Override
  public Set<Work> getWorks(UUID uuidPerson) {
    final String wTableAlias = workRepositoryImpl.getTableAlias();
    final String wTableName = workRepositoryImpl.getTableName();

    // Note: if getting list of all participating persons to work is wanted,
    // this code fragment may help as entry point:
    /*
    " e.uuid e_uuid, e.label e_label, e.refid e_refId"
    + LEFT JOIN work_creators as wc on w.uuid = wc.work_uuid
    " LEFT JOIN entities as e on e.uuid = wc.agent_uuid"
    .registerRowMapper(BeanMapper.factory(EntityImpl.class, "e"))
    if (rowView.getColumn("e_uuid", UUID.class) != null) {
      EntityImpl agent = rowView.getRow(EntityImpl.class);
      UUID agentUuid = agent.getUuid();
      List<Agent> creators = work.getCreators();
      boolean contained = false;
      for (Agent creator : creators) {
        if (agentUuid.equals(creator.getUuid())) {
          contained = true;
        }
      }
      if (!contained) {
        // FIXME: not only persons! use entityType to disambiguate!
        Person person = new PersonImpl();
        person.setLabel(agent.getLabel());
        person.setRefId(agent.getRefId());
        person.setUuid(agent.getUuid());
        work.getCreators().add(person);
      }
    }
     */
    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT * FROM "
                + wTableName
                + " AS "
                + wTableAlias
                + " LEFT JOIN work_creators AS wc ON "
                + wTableAlias
                + ".uuid = wc.work_uuid"
                + " WHERE wc.agent_uuid = :uuid"
                + " ORDER BY wc.sortIndex ASC");

    List<Work> list =
        workRepositoryImpl.retrieveList(
            workRepositoryImpl.getSqlReducedFields(), innerQuery, Map.of("uuid", uuidPerson));

    return list.stream().collect(Collectors.toSet());
  }

  @Override
  public Person save(Person person) {
    if (person.getUuid() == null) {
      person.setUuid(UUID.randomUUID());
    }
    person.setCreated(LocalDateTime.now());
    person.setLastModified(LocalDateTime.now());
    // refid is generated as serial, DO NOT SET!
    final UUID previewImageUuid =
        person.getPreviewImage() == null ? null : person.getPreviewImage().getUuid();
    final UUID locationOfBirthUuid =
        person.getPlaceOfBirth() == null ? null : person.getPlaceOfBirth().getUuid();
    final UUID locationOfDeathUuid =
        person.getPlaceOfDeath() == null ? null : person.getPlaceOfDeath().getUuid();

    String query =
        "INSERT INTO "
            + tableName
            + "("
            + "uuid, previewFileResource, label, description, preview_hints, custom_attrs,"
            + " identifiable_type, entity_type,"
            + " created, last_modified,"
            + " dateOfBirth, timeValueOfBirth,"
            + " locationOfBirth,"
            + " dateOfDeath, timeValueOfDeath,"
            + " locationOfDeath,"
            + " gender"
            + ") VALUES ("
            + ":uuid, :previewFileResource, :label::JSONB, :description::JSONB, :previewImageRenderingHints::JSONB, :customAttributes::JSONB,"
            + " :type, :entityType,"
            + " :created, :lastModified,"
            + " :dateOfBirth, :timeValueOfBirth::JSONB,"
            + " :locationOfBirth,"
            + " :dateOfDeath, :timeValueOfDeath::JSONB,"
            + " :locationOfDeath,"
            + " :gender"
            + ")";
    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bind("locationOfBirth", locationOfBirthUuid)
                .bind("locationOfDeath", locationOfDeathUuid)
                .bindBean(person)
                .execute());

    // save identifiers
    Set<Identifier> identifiers = person.getIdentifiers();
    saveIdentifiers(identifiers, person);

    // save given names
    //    List<GivenName> givenNames = person.getGivenNames();
    //    saveRelatedGivenNames(givenNames, person);
    // save family names
    //    List<FamilyName> familyNames = person.getFamilyNames();
    //    saveRelatedFamilyNames(familyNames, person);
    Person result = findOne(person.getUuid());
    return result;
  }

  //  private void saveRelatedFamilyNames(List<FamilyName> familyNames, Person person) {
  //    // we assume that relations are new (existing ones were deleted before (e.g. see update))
  //    if (familyNames != null) {
  //      dbi.useHandle(handle -> {
  //        PreparedBatch preparedBatch = handle.prepareBatch("INSERT INTO
  // rel_person_familynames(person_uuid, familyname_uuid, sortIndex) VALUES(:uuid, :familynameUuid,
  // :sortIndex)");
  //        int i = 0;
  //        for (FamilyName familyName : familyNames) {
  //          preparedBatch.bind("uuid", person.getUuid())
  //                  .bind("familynameUuid", familyName.getUuid())
  //                  .bind("sortIndex", i)
  //                  .add();
  //          i++;
  //        }
  //        preparedBatch.execute();
  //      });
  //    }
  //  }
  //  private void saveRelatedGivenNames(List<GivenName> givenNames, Person person) {
  //    // we assume that relations are new (existing ones were deleted before (e.g. see update))
  //    if (givenNames != null) {
  //      dbi.useHandle(handle -> {
  //        PreparedBatch preparedBatch = handle.prepareBatch("INSERT INTO
  // rel_person_givennames(person_uuid, givenname_uuid, sortIndex) VALUES(:uuid, :givennameUuid,
  // :sortIndex)");
  //        int i = 0;
  //        for (GivenName givenName : givenNames) {
  //          preparedBatch.bind("uuid", person.getUuid())
  //                  .bind("givennameUuid", givenName.getUuid())
  //                  .bind("sortIndex", i)
  //                  .add();
  //          i++;
  //        }
  //        preparedBatch.execute();
  //      });
  //    }
  //  }
  @Override
  public Person update(Person person) {
    person.setLastModified(LocalDateTime.now());
    final UUID previewImageUuid =
        person.getPreviewImage() == null ? null : person.getPreviewImage().getUuid();
    final UUID locationOfBirthUuid =
        person.getPlaceOfBirth() == null ? null : person.getPlaceOfBirth().getUuid();
    final UUID locationOfDeathUuid =
        person.getPlaceOfDeath() == null ? null : person.getPlaceOfDeath().getUuid();

    String query =
        "UPDATE "
            + tableName
            + " SET"
            + " previewFileResource=:previewFileResource, label=:label::JSONB, description=:description::JSONB, preview_hints=:previewImageRenderingHints::JSONB, custom_attrs=:customAttributes::JSONB,"
            + " last_modified=:lastModified,"
            + " dateOfBirth=:dateOfBirth, timeValueOfBirth=:timeValueOfBirth::JSONB,"
            + " locationOfBirth=:locationOfBirth,"
            + " dateOfDeath=:dateOfDeath, timeValueOfDeath=:timeValueOfDeath::JSONB,"
            + " locationOfDeath=:locationOfDeath,"
            + " gender=:gender"
            + " WHERE uuid=:uuid";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bind("locationOfBirth", locationOfBirthUuid)
                .bind("locationOfDeath", locationOfDeathUuid)
                .bindBean(person)
                .execute());

    // save identifiers
    // as we store the whole list new: delete old entries
    identifierRepository.deleteByIdentifiable(person);
    Set<Identifier> identifiers = person.getIdentifiers();
    saveIdentifiers(identifiers, person);

    // save given names
    //    List<GivenName> givenNames = person.getGivenNames();
    //    // as we store the whole list new: delete old entries
    //    dbi.withHandle(h -> h.createUpdate("DELETE FROM rel_person_givennames WHERE person_uuid =
    // :uuid").bind("uuid", person.getUuid()).execute());
    //    saveRelatedGivenNames(givenNames, person);
    // save family names
    //    List<FamilyName> familyNames = person.getFamilyNames();
    //    // as we store the whole list new: delete old entries
    //    dbi.withHandle(h -> h.createUpdate("DELETE FROM rel_person_familynames WHERE person_uuid =
    // :uuid").bind("uuid", person.getUuid()).execute());
    //    saveRelatedFamilyNames(familyNames, person);
    Person result = findOne(person.getUuid());
    return result;
  }
}
