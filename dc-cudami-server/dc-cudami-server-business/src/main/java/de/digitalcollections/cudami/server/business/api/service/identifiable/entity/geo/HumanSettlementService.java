package de.digitalcollections.cudami.server.business.api.service.identifiable.entity.geo;

import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.model.api.identifiable.entity.geo.HumanSettlement;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;

public interface HumanSettlementService extends IdentifiableService<HumanSettlement> {

  PageResponse<HumanSettlement> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial);
}
