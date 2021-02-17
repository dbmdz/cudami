package de.digitalcollections.cudami.server.business.api.service.identifiable.agent;

import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.model.identifiable.agent.GivenName;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;

public interface GivenNameService extends IdentifiableService<GivenName> {

  public PageResponse<GivenName> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial);
}
