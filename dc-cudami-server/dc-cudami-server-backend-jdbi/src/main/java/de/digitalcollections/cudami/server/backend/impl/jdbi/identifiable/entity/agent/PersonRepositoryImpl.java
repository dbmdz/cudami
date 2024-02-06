package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent.PersonRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.agent.FamilyNameRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.agent.GivenNameRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.DigitalObjectRepositoryImpl;
import de.digitalcollections.model.identifiable.agent.FamilyName;
import de.digitalcollections.model.identifiable.agent.GivenName;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocation;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocationType;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.validation.ValidationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.result.RowView;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class PersonRepositoryImpl extends AgentRepositoryImpl<Person> implements PersonRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(PersonRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "pe";
  public static final String TABLE_ALIAS = "p";
  public static final String TABLE_NAME = "persons";

  private final DigitalObjectRepositoryImpl digitalObjectRepositoryImpl;

  private final FamilyNameRepositoryImpl familyNameRepositoryImpl;

  private final GivenNameRepositoryImpl givenNameRepositoryImpl;

  public PersonRepositoryImpl(
      Jdbi dbi,
      CudamiConfig cudamiConfig,
      IdentifierRepository identifierRepository,
      UrlAliasRepository urlAliasRepository,
      DigitalObjectRepositoryImpl digitalObjectRepositoryImpl,
      FamilyNameRepositoryImpl familyNameRepositoryImpl,
      GivenNameRepositoryImpl givenNameRepositoryImpl) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        Person.class,
        cudamiConfig.getOffsetForAlternativePaging(),
        identifierRepository,
        urlAliasRepository);
    this.digitalObjectRepositoryImpl = digitalObjectRepositoryImpl;
    this.familyNameRepositoryImpl = familyNameRepositoryImpl;
    this.givenNameRepositoryImpl = givenNameRepositoryImpl;
  }

  @Override
  public Person create() throws RepositoryException {
    return new Person();
  }

  @Override
  protected void fullReduceRowsBiConsumer(Map<UUID, Person> map, RowView rowView) {
    super.fullReduceRowsBiConsumer(map, rowView);
    // entity should be already in map, as we here just add additional data
    Person person = map.get(rowView.getColumn(mappingPrefix + "_uuid", UUID.class));

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
      if (rowView.getColumn(GivenNameRepositoryImpl.MAPPING_PREFIX + "_uuid", UUID.class) != null) {
        person.getGivenNames().add(rowView.getRow(GivenName.class));
      }
    } catch (Exception e) {
      // TODO to avoid this, some boolean params has to be given to function, if
      // fields should
      // exist.
      LOGGER.debug("No family name or given name in rowview. Skipping.");
    }
  }

  @Override
  public String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
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
        return super.getColumnName(modelProperty);
    }
  }

  @Override
  public Set<DigitalObject> getDigitalObjects(UUID uuidPerson) throws RepositoryException {
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
  protected String getSqlInsertFields() {
    return super.getSqlInsertFields()
        + ", dateofbirth, dateofdeath, gender, locationofbirth, locationofdeath, timevalueofbirth, timevalueofdeath";
  }

  @Override
  protected String getSqlInsertValues() {
    return super.getSqlInsertValues()
        + ", :dateOfBirth, :dateOfDeath, :gender, :locationOfBirth, :locationOfDeath, :timeValueOfBirth::JSONB, :timeValueOfDeath::JSONB";
  }

  @Override
  public String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    final String familyNameMappingPrefix = FamilyNameRepositoryImpl.MAPPING_PREFIX;
    final String givenNameMappingPrefix = GivenNameRepositoryImpl.MAPPING_PREFIX;
    return super.getSqlSelectAllFields(tableAlias, mappingPrefix)
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

  @Override
  protected String getSqlSelectAllFieldsJoins() {
    return super.getSqlSelectAllFieldsJoins()
        + """
        LEFT JOIN geolocations AS glbirth ON glbirth.uuid = %1$s.locationofbirth
        LEFT JOIN geolocations AS gldeath ON gldeath.uuid = %1$s.locationofdeath
        LEFT JOIN (
          person_familynames pf INNER JOIN familynames fn ON fn.uuid = pf.familyname_uuid
        ) ON pf.person_uuid = p.uuid
        LEFT JOIN (
          person_givennames pg INNER JOIN givennames gn ON gn.uuid = pg.givenname_uuid
        ) ON pg.person_uuid = p.uuid
        """
            .formatted(tableAlias);
  }

  @Override
  public String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return super.getSqlSelectReducedFields(tableAlias, mappingPrefix)
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

  @Override
  public String getSqlUpdateFieldValues() {
    return super.getSqlUpdateFieldValues()
        + ", dateofbirth=:dateOfBirth, dateofdeath=:dateOfDeath, gender=:gender, locationofbirth=:locationOfBirth, locationofdeath=:locationOfDeath, timevalueofbirth=:timeValueOfBirth::JSONB, timevalueofdeath=:timeValueOfDeath::JSONB";
  }

  @Override
  public void save(Person person) throws RepositoryException, ValidationException {
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
  }

  private void setRelatedFamilyNames(List<FamilyName> familyNames, Person person) {
    // we assume that relations are new (existing ones were deleted before (e.g. see
    // update))
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
    // we assume that relations are new (existing ones were deleted before (e.g. see
    // update))
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
  public void update(Person person) throws RepositoryException, ValidationException {
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
  }
}
