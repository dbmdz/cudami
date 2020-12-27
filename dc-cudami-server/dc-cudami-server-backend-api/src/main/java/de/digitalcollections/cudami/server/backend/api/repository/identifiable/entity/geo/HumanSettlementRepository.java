package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.geo;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.model.api.identifiable.entity.geo.HumanSettlement;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;

public interface HumanSettlementRepository extends IdentifiableRepository<HumanSettlement> {

  HumanSettlement findOneByIdentifier(String namespace, String id);

  PageResponse<HumanSettlement> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial);
}
