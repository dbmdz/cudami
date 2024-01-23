package de.digitalcollections.cudami.server.business.api.service.identifiable.resource;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.resource.LinkedDataFileResource;
import de.digitalcollections.model.validation.ValidationException;
import java.util.List;

/** Service for LinkedDataFileResource handling. */
// FIXME: move to DigitalObjectService?
public interface DigitalObjectLinkedDataFileResourceService {

  void deleteLinkedDataFileResources(DigitalObject digitalObject) throws ServiceException;

  /**
   * Returns the list of LinkedDataFileResources for a DigitalObject
   *
   * @param digitalObject the DigitalObject
   * @return list of LinkedDataFileResources
   */
  List<LinkedDataFileResource> getLinkedDataFileResources(DigitalObject digitalObject)
      throws ServiceException;

  /**
   * Saves the list of LinkedDataFileResources for a DigitalObject
   *
   * @param digitalObject the DigitalObject
   * @param linkedDataFileResources list of LinkedDataFileResources to be persisted
   * @throws ServiceException
   * @throws ValidationException
   */
  void setLinkedDataFileResources(
      DigitalObject digitalObject, List<LinkedDataFileResource> linkedDataFileResources)
      throws ServiceException, ValidationException;
}
