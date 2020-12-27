package de.digitalcollections.cudami.server.business.api.service.identifiable.entity.geo;

import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.model.api.identifiable.entity.geo.GeoLocation;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;

public interface GeoLocationService extends IdentifiableService<GeoLocation> {

  PageResponse<GeoLocation> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial);
}
