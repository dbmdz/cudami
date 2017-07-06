package de.digitalcollections.cudami.server.business.api.service;

import de.digitalcollections.cudami.server.business.api.service.exceptions.WebsiteServiceException;
import de.digitalcollections.cudami.model.api.entity.Website;
import java.io.Serializable;
import java.util.List;
import de.digitalcollections.cudami.model.api.entity.ContentNode;

/**
 * Service for Website.
 *
 * @param <T> domain object
 * @param <ID> unique id
 */
public interface WebsiteService<T extends Website, ID extends Serializable> {

  T create();

  T save(T website) throws WebsiteServiceException;

  T update(T website) throws WebsiteServiceException;

  T get(ID id) throws WebsiteServiceException;

  List<T> getAll();

//  T find(UUID uuid);
  
  List<ContentNode> getRootCategories(T website);
}
