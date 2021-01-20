package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent.PersonRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.EntityRepositoryImpl;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.entity.agent.Agent;
import de.digitalcollections.model.api.identifiable.entity.agent.Person;
import de.digitalcollections.model.api.identifiable.entity.geo.enums.GeoLocationType;
import de.digitalcollections.model.api.identifiable.entity.work.Work;
import de.digitalcollections.model.api.identifiable.parts.LocalizedText;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.IdentifierImpl;
import de.digitalcollections.model.impl.identifiable.entity.DigitalObjectImpl;
import de.digitalcollections.model.impl.identifiable.entity.EntityImpl;
import de.digitalcollections.model.impl.identifiable.entity.agent.PersonImpl;
import de.digitalcollections.model.impl.identifiable.entity.geo.GeoLocationImpl;
import de.digitalcollections.model.impl.identifiable.entity.work.WorkImpl;
import de.digitalcollections.model.impl.identifiable.resource.ImageFileResourceImpl;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.jdbi.v3.core.result.RowView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class PersonRepositoryImpl extends EntityRepositoryImpl<PersonImpl>
        implements PersonRepository<PersonImpl> {

  private static final Logger LOGGER = LoggerFactory.getLogger(PersonRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "pe";
  
  public static final String SQL_REDUCED_FIELDS_PE
          = " p.uuid pe_uuid, p.label pe_label, p.description pe_description, p.refid pe_refId,"
          + " p.identifiable_type pe_type, p.entity_type pe_entityType,"
          + " p.created pe_created, p.last_modified pe_lastModified,"
          + " p.dateOfBirth pe_dateOfBirth, p.timevalueofbirth pe_timeValueOfBirth,"
          + " p.dateOfDeath pe_dateOfDeath, p.timevalueofdeath pe_timeValueOfDeath,"
          + " p.gender pe_gender";

  private static final String SQL_FULL_FIELDS_JOINS =
          " LEFT JOIN geolocations AS glbirth ON glbirth.uuid = p.locationofbirth"
            + " LEFT JOIN geolocations AS gldeath ON gldeath.uuid = p.locationofdeath";
  
  public static final String SQL_FULL_FIELDS_PE = SQL_REDUCED_FIELDS_PE
          + ", glbirth.uuid glbirth_uuid, glbirth.label glbirth_label, glbirth.geolocation_type glbirth_geoLocationType,"
          + " gldeath.uuid gldeath_uuid, gldeath.label gldeath_label, gldeath.geolocation_type gldeath_geoLocationType";

  public static final String TABLE_ALIAS = "p";
  public static final String TABLE_NAME = "persons";

  @Autowired
  public PersonRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository) {
    super(
            dbi,
            identifierRepository,
            TABLE_NAME,
            TABLE_ALIAS,
            MAPPING_PREFIX,
            PersonImpl.class,
            SQL_REDUCED_FIELDS_PE,
            SQL_FULL_FIELDS_PE,
            SQL_FULL_FIELDS_JOINS,
            createAdditionalReduceRowsBiFunction());
  }

  private static BiFunction<LinkedHashMap<UUID, PersonImpl>, RowView, LinkedHashMap<UUID, PersonImpl>> createAdditionalReduceRowsBiFunction() {
    return (map, rowView) -> {
      // entity should be already in map, as we here just add additional data
      PersonImpl person = map.get(rowView.getColumn(MAPPING_PREFIX + "_uuid", UUID.class));

      if (rowView.getColumn("glbirth_uuid", UUID.class) != null) {
        UUID glBirthUuid = rowView.getColumn("glbirth_uuid", UUID.class);
        LocalizedText label = rowView.getColumn("glbirth_label", LocalizedText.class);
        GeoLocationType geoLocationType = rowView.getColumn("glbirth_geoLocationType", GeoLocationType.class);
        final GeoLocationImpl placeOfBirth = new GeoLocationImpl();
        placeOfBirth.setUuid(glBirthUuid);
        placeOfBirth.setLabel(label);
        placeOfBirth.setGeoLocationType(geoLocationType);
        person.setPlaceOfBirth(placeOfBirth);
      }
      
      if (rowView.getColumn("gldeath_uuid", UUID.class) != null) {
        UUID glDeathUuid = rowView.getColumn("gldeath_uuid", UUID.class);
        LocalizedText label = rowView.getColumn("gldeath_label", LocalizedText.class);
        GeoLocationType geoLocationType = rowView.getColumn("gldeath_geoLocationType", GeoLocationType.class);
        final GeoLocationImpl placeOfDeath = new GeoLocationImpl();
        placeOfDeath.setUuid(glDeathUuid);
        placeOfDeath.setLabel(label);
        placeOfDeath.setGeoLocationType(geoLocationType);
        person.setPlaceOfDeath(placeOfDeath);
      }

      return map;
    };
  }

  @Override
  public PageResponse<PersonImpl> findByLocationOfBirth(PageRequest pageRequest, UUID uuidGeoLocation) {
    throw new UnsupportedOperationException(
            "Not supported yet."); // To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public PageResponse<PersonImpl> findByLocationOfDeath(PageRequest pageRequest, UUID uuidGeoLocation) {
    throw new UnsupportedOperationException(
            "Not supported yet."); // To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Set<DigitalObject> getDigitalObjects(UUID uuidPerson) {
    String query
            = "SELECT d.uuid d_uuid, d.label d_label, d.refid d_refId,"
            + " file.uuid pf_uuid, file.filename pf_filename, file.mimetype pf_mimetype, file.size_in_bytes pf_sizeInBytes, file.uri pf_uri, file.http_base_url pf_httpBaseUrl"
            + " FROM digitalobjects as d"
            + " LEFT JOIN item_digitalobjects as itdi on d.uuid = itdi.digitalobject_uuid"
            + " LEFT JOIN item_works as itwo on itdi.item_uuid = itwo.item_uuid"
            + " LEFT JOIN work_creators as wocr on itwo.work_uuid = wocr.work_uuid"
            + " LEFT JOIN fileresources_image as file on d.previewfileresource = file.uuid"
            + " WHERE wocr.agent_uuid = :uuid";

    Set<DigitalObject> result
            = dbi.withHandle(
                    h
                    -> h
                            .createQuery(query)
                            .bind("uuid", uuidPerson)
                            .registerRowMapper(BeanMapper.factory(DigitalObjectImpl.class, "d"))
                            .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pf"))
                            .reduceRows(
                                    new LinkedHashMap<UUID, DigitalObject>(),
                                    (map, rowView) -> {
                                      DigitalObject digitalObject
                                      = map.computeIfAbsent(
                                              rowView.getColumn("d_uuid", UUID.class),
                                              fn -> {
                                                return rowView.getRow(DigitalObjectImpl.class);
                                              });
                                      if (rowView.getColumn("pf_uuid", UUID.class) != null) {
                                        digitalObject.setPreviewImage(
                                                rowView.getRow(ImageFileResourceImpl.class));
                                      }
                                      return map;
                                    })
                            .values()
                            .stream()
                            .collect(Collectors.toSet()));
    return result;
  }

  @Override
  public Set<Work> getWorks(UUID uuidPerson) {
    String query
            = "SELECT w.uuid w_uuid, w.label w_label, w.refid w_refId,"
            + " w.created w_created, w.last_modified w_lastModified,"
            + " w.date_published w_datePublished, w.timevalue_published w_timeValuePublished,"
            + " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id,"
            + " file.uuid pf_uuid, file.filename pf_filename, file.mimetype pf_mimetype, file.size_in_bytes pf_size_in_bytes, file.uri pf_uri,"
            + " e.uuid e_uuid, e.label e_label, e.refid e_refId"
            + " FROM works as w"
            + " LEFT JOIN identifiers as id on w.uuid = id.identifiable"
            + " LEFT JOIN work_creators as wc on w.uuid = wc.work_uuid"
            + " LEFT JOIN entities as e on e.uuid = wc.agent_uuid"
            + " LEFT JOIN fileresources_image as file on w.previewfileresource = file.uuid"
            + " WHERE wc.agent_uuid = :uuid"
            + " ORDER BY wc.sortIndex ASC";

    Set<Work> result
            = dbi.withHandle(
                    h
                    -> h
                            .createQuery(query)
                            .bind("uuid", uuidPerson)
                            .registerRowMapper(BeanMapper.factory(WorkImpl.class, "w"))
                            .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                            .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pf"))
                            .registerRowMapper(BeanMapper.factory(EntityImpl.class, "e"))
                            .reduceRows(
                                    new LinkedHashMap<UUID, PersonImpl>(),
                                    (map, rowView) -> {
                                      mapRowToIdentifiable(true, true).apply(map, rowView);
                                      mapAdditional.apply(map, rowView);
                                      return map;
                                      Work work
                                      = map.computeIfAbsent(
                                              rowView.getColumn("w_uuid", UUID.class),
                                              fn -> {
                                                return rowView.getRow(WorkImpl.class);
                                              });
                                      if (rowView.getColumn("pf_uuid", UUID.class) != null) {
                                        work.setPreviewImage(rowView.getRow(ImageFileResourceImpl.class));
                                      }
                                      if (rowView.getColumn("id_uuid", UUID.class) != null) {
                                        IdentifierImpl dbIdentifier = rowView.getRow(IdentifierImpl.class);
                                        work.addIdentifier(dbIdentifier);
                                      }
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
                                      return map;
                                    })
                            .values()
                            .stream()
                            .collect(Collectors.toSet()));
    return result;
  }

  public BiFunction<LinkedHashMap<UUID, PersonImpl>, RowView, LinkedHashMap<UUID, PersonImpl>> mapAdditional() {
    return (map, rowView) -> {
      PersonImpl person
              = map.computeIfAbsent(
                      rowView.getColumn(mappingPrefix + "_uuid", UUID.class),
                      fn -> {
                        return rowView.getRow(identifiableImplClass);
                      });

      // additional object mappings: location of birth, location of death
//      if (rowView.getColumn("pi_uuid", UUID.class) != null) {
//        person.setPreviewImage(rowView.getRow(ImageFileResourceImpl.class));
//      }
//      if (rowView.getColumn("id_uuid", UUID.class) != null) {
//        IdentifierImpl dbIdentifier = rowView.getRow(IdentifierImpl.class);
//        person.addIdentifier(dbIdentifier);
//      }
      return map;
    };
  }

  LinkedHashMap<UUID, PersonImpl> mapRowToPerson(LinkedHashMap<UUID, PersonImpl> map, RowView rowView) {
    // https://stackoverflow.com/questions/26679628/how-to-chain-bifunctions
    return mapRowToIdentifiable(true, true)
            .andThen(m -> mapAdditional().apply(m, rowView))
            .apply(map, rowView);
  }

  @Override
  public Person save(Person person) {
    if (person.getUuid() == null) {
      person.setUuid(UUID.randomUUID());
    }
    person.setCreated(LocalDateTime.now());
    person.setLastModified(LocalDateTime.now());

    final UUID previewImageUuid
            = person.getPreviewImage() == null ? null : person.getPreviewImage().getUuid();
    //    final UUID locationOfBirthUuid = person.getPlaceOfBirth() == null ? null :
    // person.getPlaceOfBirth().getUuid();
    //    final UUID locationOfDeathUuid = person.getPlaceOfDeath() == null ? null :
    // person.getPlaceOfDeath().getUuid();

    String query
            = "INSERT INTO persons("
            + "uuid, previewFileResource, label, description,"
            + " identifiable_type, entity_type,"
            + " created, last_modified,"
            + " dateOfBirth, timeValueOfBirth,"
            //            + " locationOfBirth,"
            + " dateOfDeath, timeValueOfDeath,"
            //            + " locationOfDeath,"
            + " gender"
            + ") VALUES ("
            + ":uuid, :previewFileResource, :label::JSONB, :description::JSONB,"
            + " :type, :entityType,"
            + " :created, :lastModified,"
            + " :dateOfBirth, :timeValueOfBirth::JSONB,"
            //            + " :locationOfBirth,"
            + " :dateOfDeath, :timeValueOfDeath::JSONB,"
            //            + " :locationOfDeath,"
            + " :gender"
            + ")";
    dbi.withHandle(
            h
            -> h.createUpdate(query)
                    .bind("previewFileResource", previewImageUuid)
                    //            .bind("locationOfBirth", locationOfBirthUuid)
                    //            .bind("locationOfDeath", locationOfDeathUuid)
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

  //  private void saveIdentifiers(List<Identifier> identifiers, Person person) {
  //    // we assume that identifiers (unique to object) are new (existing ones were deleted before
  //    // (e.g. see update))
  //    if (identifiers != null) {
  //      for (Identifier identifier : identifiers) {
  //        identifier.setIdentifiable(person.getUuid());
  //        identifierRepository.save(identifier);
  //      }
  //    }
  //  }
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
    final UUID previewImageUuid
            = person.getPreviewImage() == null ? null : person.getPreviewImage().getUuid();
    //    final UUID locationOfBirthUuid = person.getPlaceOfBirth() == null ? null :
    // person.getPlaceOfBirth().getUuid();
    //    final UUID locationOfDeathUuid = person.getPlaceOfDeath() == null ? null :
    // person.getPlaceOfDeath().getUuid();

    String query
            = "UPDATE persons SET"
            + " previewFileResource=:previewFileResource, label=:label::JSONB, description=:description::JSONB,"
            + " last_modified=:lastModified,"
            + " dateOfBirth=:dateOfBirth, timeValueOfBirth=:timeValueOfBirth::JSONB,"
            //            + " locationOfBirth=:locationOfBirth,"
            + " dateOfDeath=:dateOfDeath, timeValueOfDeath=:timeValueOfDeath::JSONB,"
            //            + " locationOfDeath=:locationOfDeath,"
            + " gender=:gender"
            + " WHERE uuid=:uuid";

    dbi.withHandle(
            h
            -> h.createUpdate(query)
                    .bind("previewFileResource", previewImageUuid)
                    //            .bind("locationOfBirth", locationOfBirthUuid)
                    //            .bind("locationOfDeath", locationOfDeathUuid)
                    .bindBean(person)
                    .execute());

    // save identifiers
    Set<Identifier> identifiers = person.getIdentifiers();
    // as we store the whole list new: delete old entries
    dbi.withHandle(
            h
            -> h.createUpdate("DELETE FROM identifiers WHERE identifiable = :uuid")
                    .bind("uuid", person.getUuid())
                    .execute());
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
