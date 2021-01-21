package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.geo;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.model.api.identifiable.entity.geo.HumanSettlement;

/**
 * Repository for HumanSettlement persistence handling.
 *
 * @param <H> instance of human settlement implementation
 */
public interface HumanSettlementRepository<H extends HumanSettlement> extends EntityRepository<H> {}
