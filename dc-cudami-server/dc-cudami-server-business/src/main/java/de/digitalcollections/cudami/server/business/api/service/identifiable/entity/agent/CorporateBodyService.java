package de.digitalcollections.cudami.server.business.api.service.identifiable.entity.agent;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.validation.ValidationException;
import java.util.List;

public interface CorporateBodyService extends AgentService<CorporateBody> {

  /**
   * Get GND data from an official GND-source and save as new filled entity.
   *
   * @param gndId GND-ID of entity, e.g. "2007744-0" (DFG)
   * @return filled and saved entity instance
   * @throws ServiceException
   * @throws ValidationException
   */
  CorporateBody fetchAndSaveByGndId(String gndId) throws ServiceException, ValidationException;

  List<CorporateBody> findCollectionRelatedCorporateBodies(
      Collection collection, Filtering filtering) throws ServiceException;
}
