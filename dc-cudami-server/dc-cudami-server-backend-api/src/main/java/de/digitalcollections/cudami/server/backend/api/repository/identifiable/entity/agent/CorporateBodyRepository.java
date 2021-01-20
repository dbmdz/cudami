package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.model.api.identifiable.entity.agent.CorporateBody;

/** Repository for CorporateBody persistence handling.
 * @param <C> instance of corporate body implementation */
public interface CorporateBodyRepository<C extends CorporateBody> extends EntityRepository<C> {}
