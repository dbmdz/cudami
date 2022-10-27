package de.digitalcollections.cudami.server.backend.api.repository;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.model.identifiable.entity.work.Publisher;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.UUID;

public interface PublisherRepository {

  /**
   * Finds and returns a page list of publishers
   *
   * @param pageRequest the PageRequest
   * @return PageResponse
   * @throws RepositoryException in case of an error
   */
  PageResponse<Publisher> find(PageRequest pageRequest) throws RepositoryException;

  /**
   * Retrieves a Publisher by its uuid
   *
   * @param uuid the UUID of the Publisher
   * @return Publisher or null
   * @throws RepositoryException in case of an error
   */
  Publisher getByUuid(UUID uuid) throws RepositoryException;

  /**
   * Creates and persists a Publisher, which did not exist before
   *
   * @param publisher the Publisher to be created and persisted (without UUID)
   * @return the persisted Publisher with its UUID
   * @throws RepositoryException in case of an error
   */
  Publisher save(Publisher publisher) throws RepositoryException;

  /**
   * Updates an existing Publisher
   *
   * @param publisher the Publisher to be updated (identified by its UUID)
   * @return the updated Publisher
   * @throws RepositoryException in case of an error
   */
  Publisher update(Publisher publisher) throws RepositoryException;

  /**
   * Delete an existing Publisher
   *
   * @param uuid the UUID of the existing Publisher
   * @return success value (number of deleted Publishers)
   * @throws RepositoryException in case of an error
   */
  int deleteByUuid(UUID uuid) throws RepositoryException;
}
