package de.digitalcollections.cudami.server.business.api.service.identifiable.entity;

import de.digitalcollections.model.api.identifiable.entity.Corporation;

public interface CorporationService extends EntityService<Corporation> {

  /**
   * Get GND data from an official GND-source and save as new filled entity.
   *
   * @param gndId GND-ID of entity, e.g. "2007744-0" (DFG)
   * @return filled and saved entity instance
   */
  Corporation fetchAndSaveByGndId(String gndId);
}
