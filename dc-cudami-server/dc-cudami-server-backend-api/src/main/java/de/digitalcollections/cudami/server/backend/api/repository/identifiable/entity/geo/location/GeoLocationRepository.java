package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.geo.location;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocation;

/** Repository for GeoLocation persistence handling. */
public interface GeoLocationRepository<G extends GeoLocation> extends EntityRepository<G> {}
