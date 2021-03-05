package de.digitalcollections.cudami.server.business.api.service.identifiable.agent;

import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.model.identifiable.agent.FamilyName;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;

public interface FamilyNameService extends IdentifiableService<FamilyName> {

  public PageResponse<FamilyName> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial);
}
