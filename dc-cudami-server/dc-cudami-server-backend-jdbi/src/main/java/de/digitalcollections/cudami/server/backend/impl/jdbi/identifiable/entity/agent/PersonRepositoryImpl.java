package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent.PersonRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.agent.FamilyNameRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.agent.GivenNameRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.DigitalObjectRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.EntityRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.work.WorkRepositoryImpl;
import de.digitalcollections.model.identifiable.agent.FamilyName;
import de.digitalcollections.model.identifiable.agent.GivenName;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocation;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocationType;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.text.LocalizedText;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.result.RowView;
import org.jdbi.v3.core.statement.PreparedBatch;
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
          + ".locationofdeath"
          + " LEFT JOIN person_familynames AS pf ON pf.person_uuid = p.uuid"
          + " LEFT JOIN familynames AS fn ON fn.uuid = pf.familyname_uuid"
          + " LEFT JOIN person_givennames AS pg ON pg.person_uuid = p.uuid"
          + " LEFT JOIN givennames AS gn ON gn.uuid = pg.givenname_uuid";
  public static final String TABLE_NAME = "persons";

  private static BiFunction<Map<UUID, Person>, RowView, Map<UUID, Person>>
      createAdditionalReduceRowsBiFunction() {
    return (map, rowView) -> {
      // entity should be already in map, as we here just add additional data
      Person person = map.get(rowView.getColumn(MAPPING_PREFIX + "_uuid", UUID.class));

      if (rowView.getColumn("glbirth_uuid", UUID.class) != null) {
        UUID glBirthUuid = rowView.getColumn("glbirth_uuid", UUID.class);
        Long glRefId = rowView.getColumn("glbirth_refid", Long.class);
        LocalizedText label = rowView.getColumn("glbirth_label", LocalizedText.class);
        GeoLocationType geoLocationType =
            rowView.getColumn("glbirth_geoLocationType", GeoLocationType.class);
        final GeoLocation placeOfBirth = new GeoLocation();
        placeOfBirth.setUuid(glBirthUuid);
        placeOfBirth.setRefId(glRefId);
        placeOfBirth.setLabel(label);
        placeOfBirth.setGeoLocationType(geoLocationType);
        person.setPlaceOfBirth(placeOfBirth);
      }

      if (rowView.getColumn("gldeath_uuid", UUID.class) != null) {
        UUID glDeathUuid = rowView.getColumn("gldeath_uuid", UUID.class);
        Long glRefId = rowView.getColumn("gldeath_refid", Long.class);
        LocalizedText label = rowView.getColumn("gldeath_label", LocalizedText.class);
        GeoLocationType geoLocationType =
            rowView.getColumn("gldeath_geoLocationType", GeoLocationType.class);
        final GeoLocation placeOfDeath = new GeoLocation();
        placeOfDeath.setUuid(glDeathUuid);
        placeOfDeath.setRefId(glRefId);
        placeOfDeath.setLabel(label);
        placeOfDeath.setGeoLocationType(geoLocationType);
        person.setPlaceOfDeath(placeOfDeath);
      }

      try {
        if (rowView.getColumn(FamilyNameRepositoryImpl.MAPPING_PREFIX + "_uuid", UUID.class)
            != null) {
          person.getFamilyNames().add(rowView.getRow(FamilyName.class));
        }
        if (rowView.getColumn(GivenNameRepositoryImpl.MAPPING_PREFIX + "_uuid", UUID.class)
            != null) {
          person.getGivenNames().add(rowView.getRow(GivenName.class));
        }
      } catch (Exception e) {
        // TODO to avoid this, some boolean params has to be given to function, if fields should
        // exist.
        LOGGER.debug("No family name or given name in rowview. Skipping.");
      }
      return map;
    };
  }

  public static String getSqlInsertFields() {
    return EntityRepositoryImpl.getSqlInsertFields()
        + ", dateofbirth, dateofdeath, gender, locationofbirth, locationofdeath, timevalueofbirth, timevalueofdeath";
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  public static String getSqlInsertValues() {
    return EntityRepositoryImpl.getSqlInsertValues()
        + ", :dateOfBirth, :dateOfDeath, :gender, :locationOfBirth, :locationOfDeath, :timeValueOfBirth::JSONB, :timeValueOfDeath::JSONB";
  }

  public static String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    final String familyNameMappingPrefix = FamilyNameRepositoryImpl.MAPPING_PREFIX;
    final String givenNameMappingPrefix = GivenNameRepositoryImpl.MAPPING_PREFIX;
    return getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
        + "glbirth.uuid glbirth_uuid, glbirth.refId glbirth_refid, glbirth.label glbirth_label, glbirth.geolocation_type glbirth_geoLocationType, "
        + "gldeath.uuid gldeath_uuid, gldeath.refId gldeath_refid, gldeath.label gldeath_label, gldeath.geolocation_type gldeath_geoLocationType, "
        + "fn.uuid "
        + familyNameMappingPrefix
        + "_uuid, "
        + "fn.label "
        + familyNameMappingPrefix
        + "_label, "
        + "gn.uuid "
        + givenNameMappingPrefix
        + "_uuid, "
        + "gn.label "
        + givenNameMappingPrefix
        + "_label";
  }

  public static String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return EntityRepositoryImpl.getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
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
        + ".timevalueofbirth "
        + mappingPrefix
        + "_timeValueOfBirth, "
        + tableAlias
        + ".timevalueofdeath "
        + mappingPrefix
        + "_timeValueOfDeath";
  }

  public static String getSqlUpdateFieldValues() {
    return EntityRepositoryImpl.getSqlUpdateFieldValues()
        + ", dateofbirth=:dateOfBirth, dateofdeath=:dateOfDeath, gender=:gender, locationofbirth=:locationOfBirth, locationofdeath=:locationOfDeath, timevalueofbirth=:timeValueOfBirth::JSONB, timevalueofdeath=:timeValueOfDeath::JSONB";
  }

  private final DigitalObjectRepositoryImpl digitalObjectRepositoryImpl;
  private final FamilyNameRepositoryImpl familyNameRepositoryImpl;
  private final GivenNameRepositoryImpl givenNameRepositoryImpl;
  private final WorkRepositoryImpl workRepositoryImpl;

  @Autowired
  public PersonRepositoryImpl(
      Jdbi dbi,
      DigitalObjectRepositoryImpl digitalObjectRepositoryImpl,
      FamilyNameRepositoryImpl familyNameRepositoryImpl,
      GivenNameRepositoryImpl givenNameRepositoryImpl,
      WorkRepositoryImpl workRepositoryImpl,
      CudamiConfig cudamiConfig) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        Person.class,
        getSqlSelectAllFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlSelectReducedFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlInsertFields(),
        getSqlInsertValues(),
        getSqlUpdateFieldValues(),
        SQL_FULL_FIELDS_JOINS,
        createAdditionalReduceRowsBiFunction(),
        cudamiConfig.getOffsetForAlternativePaging());
    this.digitalObjectRepositoryImpl = digitalObjectRepositoryImpl;
    this.familyNameRepositoryImpl = familyNameRepositoryImpl;
    this.givenNameRepositoryImpl = givenNameRepositoryImpl;
    this.workRepositoryImpl = workRepositoryImpl;
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
      case "dateOfBirth":
        return tableAlias + ".dateofbirth";
      case "dateOfDeath":
        return tableAlias + ".dateofdeath";
      case "gender":
        return tableAlias + ".gender";
      case "placeOfBirth":
        return tableAlias + ".locationofbirth";
      case "placeOfDeath":
        return tableAlias + ".locationofdeath";
      case "timeValueOfBirth":
        return tableAlias + ".timevalueofbirth";
      case "timeValueOfDeath":
        return tableAlias + ".timevalueofdeath";
      default:
        return null;
    }
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
    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("uuid", uuidPerson);

    List<DigitalObject> list =
        digitalObjectRepositoryImpl.retrieveList(
            digitalObjectRepositoryImpl.getSqlSelectReducedFields(),
            innerQuery,
            argumentMappings,
            null);

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
            "SELECT wc.sortindex AS idx, * FROM "
                + wTableName
                + " AS "
                + wTableAlias
                + " LEFT JOIN work_creators AS wc ON "
                + wTableAlias
                + ".uuid = wc.work_uuid"
                + " WHERE wc.agent_uuid = :uuid"
                + " ORDER BY idx ASC");
    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("uuid", uuidPerson);

    List<Work> list =
        workRepositoryImpl.retrieveList(
            workRepositoryImpl.getSqlSelectReducedFields(),
            innerQuery,
            argumentMappings,
            "ORDER BY idx ASC");

    return list.stream().collect(Collectors.toSet());
  }

  @Override
  public Person save(Person person) {
    final UUID locationOfBirthUuid =
        person.getPlaceOfBirth() == null ? null : person.getPlaceOfBirth().getUuid();
    final UUID locationOfDeathUuid =
        person.getPlaceOfDeath() == null ? null : person.getPlaceOfDeath().getUuid();
    Map<String, Object> bindings = new HashMap<>();
    bindings.put("locationOfBirth", locationOfBirthUuid);
    bindings.put("locationOfDeath", locationOfDeathUuid);
    super.save(person, bindings);

    // save given names
    List<GivenName> givenNames = person.getGivenNames();
    setRelatedGivenNames(givenNames, person);
    // save family names
    List<FamilyName> familyNames = person.getFamilyNames();
    setRelatedFamilyNames(familyNames, person);
    Person result = getByUuid(person.getUuid());
    return result;
  }

  private void setRelatedFamilyNames(List<FamilyName> familyNames, Person person) {
    // we assume that relations are new (existing ones were deleted before (e.g. see update))
    if (familyNames != null) {
      dbi.useHandle(
          handle -> {
            PreparedBatch preparedBatch =
                handle.prepareBatch(
                    "INSERT INTO person_familynames(person_uuid, familyname_uuid, sortIndex) VALUES(:uuid, :familynameUuid, :sortIndex)");
            int i = 0;
            for (FamilyName familyName : familyNames) {
              preparedBatch
                  .bind("uuid", person.getUuid())
                  .bind("familynameUuid", familyName.getUuid())
                  .bind("sortIndex", i)
                  .add();
              i++;
            }
            preparedBatch.execute();
          });
    }
  }

  private void setRelatedGivenNames(List<GivenName> givenNames, Person person) {
    // we assume that relations are new (existing ones were deleted before (e.g. see update))
    if (givenNames != null) {
      dbi.useHandle(
          handle -> {
            PreparedBatch preparedBatch =
                handle.prepareBatch(
                    "INSERT INTO person_givennames(person_uuid, givenname_uuid, sortIndex) VALUES(:uuid, :givennameUuid, :sortIndex)");
            int i = 0;
            for (GivenName givenName : givenNames) {
              preparedBatch
                  .bind("uuid", person.getUuid())
                  .bind("givennameUuid", givenName.getUuid())
                  .bind("sortIndex", i)
                  .add();
              i++;
            }
            preparedBatch.execute();
          });
    }
  }

  @Override
  public Person update(Person person) {
    final UUID locationOfBirthUuid =
        person.getPlaceOfBirth() == null ? null : person.getPlaceOfBirth().getUuid();
    final UUID locationOfDeathUuid =
        person.getPlaceOfDeath() == null ? null : person.getPlaceOfDeath().getUuid();
    Map<String, Object> bindings = new HashMap<>();
    bindings.put("locationOfBirth", locationOfBirthUuid);
    bindings.put("locationOfDeath", locationOfDeathUuid);
    super.update(person, bindings);

    // save given names
    List<GivenName> givenNames = person.getGivenNames();
    // as we store the whole list new: delete old entries
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM person_givennames WHERE person_uuid = :uuid")
                .bind("uuid", person.getUuid())
                .execute());
    setRelatedGivenNames(givenNames, person);
    // save family names
    List<FamilyName> familyNames = person.getFamilyNames();
    // as we store the whole list new: delete old entries
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM person_familynames WHERE person_uuid = :uuid")
                .bind("uuid", person.getUuid())
                .execute());
    setRelatedFamilyNames(familyNames, person);
    Person result = getByUuid(person.getUuid());
    return result;
  }
}
