package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent;

import de.digitalcollections.model.api.identifiable.entity.agent.CorporateBody;

/** Repository for external CorporateBody persistence handling. */
public interface ExternalCorporateBodyRepository {

  /**
   * Get GND data from an official GND-source and return as new filled entity.
   *
   * @param gndId GND-ID of entity, e.g. "2007744-0" (DFG)
   * @return filled entity instance
   */
  CorporateBody getByGndId(String gndId);
}
