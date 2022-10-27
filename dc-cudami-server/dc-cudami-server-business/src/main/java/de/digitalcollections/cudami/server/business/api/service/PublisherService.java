package de.digitalcollections.cudami.server.business.api.service;

import de.digitalcollections.cudami.server.business.api.service.exceptions.CudamiServiceException;
import de.digitalcollections.model.identifiable.entity.work.Publisher;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.UUID;

/** Service to handle Publishers */
public interface PublisherService {

  /**
   * Retrieve Publishers
   *
   * @param pageRequest the PageRequest
   * @return A PageResponse with matching Publishers
   * @throws CudamiServiceException in case of an error
   */
  PageResponse<Publisher> find(PageRequest pageRequest) throws CudamiServiceException;

  /**
   * Retrieve a single Publisher by its uuid
   *
   * @param uuid the UUID of the Publisher
   * @return the Publisher or null
   * @throws CudamiServiceException in case of an error
   */
  Publisher getByUuid(UUID uuid) throws CudamiServiceException;

  /**
   * Saves a newly created Publisher
   *
   * @param publisher the newly created Publisher (without UUID) which must not yet exist
   * @return the Publisher with UUID
   * @throws CudamiServiceException in case of an error
   */
  Publisher save(Publisher publisher) throws CudamiServiceException;

  /**
   * Updates an existing Publisher
   *
   * @param publisher the already existing Publisher which shall be updated
   * @return the updated Publisher
   * @throws CudamiServiceException in case of an error
   */
  Publisher update(Publisher publisher) throws CudamiServiceException;

  /**
   * Deletes an existing Publisher
   *
   * @param uuid the UUID of the existing Publisher
   * @return boolean value for successful deletion (hence false, if the Publisher did not exist
   *     before)
   * @throws CudamiServiceException in case of an error
   */
  boolean delete(UUID uuid) throws CudamiServiceException;
}
