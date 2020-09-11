package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

import de.digitalcollections.model.api.identifiable.entity.Corporation;

/** Repository for Corporation persistence handling. */
public interface ExternalCorporationRepository {

  /**
   * Get GND data from an official GND-source and return as new filled entity.
   *
   * @param gndId GND-ID of entity, e.g. "2007744-0" (DFG)
   * @return filled entity instance
   */
  Corporation getByGndId(String gndId);
}
