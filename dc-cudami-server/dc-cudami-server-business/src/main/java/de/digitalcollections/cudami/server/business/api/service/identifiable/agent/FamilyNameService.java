package de.digitalcollections.cudami.server.business.api.service.identifiable.agent;

import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.model.api.identifiable.agent.FamilyName;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;

public interface FamilyNameService extends IdentifiableService<FamilyName> {

  public PageResponse<FamilyName> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial);
}
