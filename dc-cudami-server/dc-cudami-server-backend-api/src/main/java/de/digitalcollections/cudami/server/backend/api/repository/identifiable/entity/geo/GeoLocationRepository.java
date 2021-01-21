package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.geo;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.model.api.identifiable.entity.geo.GeoLocation;

/**
 * Repository for GeoLocation persistence handling.
 *
 * @param <G> instance of geolocation implementation
 */
public interface GeoLocationRepository<G extends GeoLocation> extends EntityRepository<G> {}
