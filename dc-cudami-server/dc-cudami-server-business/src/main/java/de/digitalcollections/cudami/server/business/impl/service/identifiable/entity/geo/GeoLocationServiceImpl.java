package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.geo;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.geo.GeoLocationRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.geo.GeoLocationService;
import de.digitalcollections.cudami.server.business.impl.service.identifiable.IdentifiableServiceImpl;
import de.digitalcollections.model.api.identifiable.entity.geo.GeoLocation;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GeoLocationServiceImpl extends IdentifiableServiceImpl<GeoLocation>
    implements GeoLocationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(GeoLocationServiceImpl.class);

  @Autowired
  public GeoLocationServiceImpl(GeoLocationRepository repository) {
    super(repository);
  }

  @Override
  public PageResponse<GeoLocation> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial) {
    PageResponse<GeoLocation> result =
        ((GeoLocationRepository) repository)
            .findByLanguageAndInitial(pageRequest, language, initial);
    return result;
  }
}
