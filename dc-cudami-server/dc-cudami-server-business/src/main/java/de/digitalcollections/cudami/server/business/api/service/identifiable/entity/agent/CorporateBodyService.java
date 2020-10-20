package de.digitalcollections.cudami.server.business.api.service.identifiable.entity.agent;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.EntityService;
import de.digitalcollections.model.api.identifiable.entity.agent.CorporateBody;

public interface CorporateBodyService extends EntityService<CorporateBody> {

  /**
   * Get GND data from an official GND-source and save as new filled entity.
   *
   * @param gndId GND-ID of entity, e.g. "2007744-0" (DFG)
   * @return filled and saved entity instance
   */
  CorporateBody fetchAndSaveByGndId(String gndId);
}
