package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.geo;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.model.api.identifiable.entity.geo.GeoLocation;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;

public interface GeoLocationRepository extends IdentifiableRepository<GeoLocation> {

  GeoLocation findOneByIdentifier(String namespace, String id);

  PageResponse<GeoLocation> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial);
}
