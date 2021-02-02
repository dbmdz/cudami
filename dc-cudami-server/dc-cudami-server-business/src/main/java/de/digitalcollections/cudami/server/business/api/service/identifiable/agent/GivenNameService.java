package de.digitalcollections.cudami.server.business.api.service.identifiable.agent;

import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.model.api.identifiable.agent.GivenName;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;

public interface GivenNameService extends IdentifiableService<GivenName> {

  public PageResponse<GivenName> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial);
}
