package de.digitalcollections.cudami.server.business.api.service.identifiable.entity.agent;

import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocation;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;

public interface PersonService extends AgentService<Person> {

  PageResponse<Person> findByGeoLocationOfBirth(GeoLocation geoLocation, PageRequest pageRequest);

  PageResponse<Person> findByGeoLocationOfDeath(GeoLocation geoLocation, PageRequest pageRequest);
}
