package de.digitalcollections.cudami.server.business.api.service.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.identifiable.NodeService;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public interface CollectionService extends NodeService<Collection>, EntityService<Collection> {

  default boolean addDigitalObject(Collection collection, DigitalObject digitalObject) {
    if (collection == null || digitalObject == null) {
      return false;
    }
    return addDigitalObjects(collection.getUuid(), Arrays.asList(digitalObject));
  }

  default boolean addDigitalObjects(Collection collection, List<DigitalObject> digitalObjects) {
    if (collection == null || digitalObjects == null) {
      return false;
    }
    return addDigitalObjects(collection.getUuid(), digitalObjects);
  }

  boolean addDigitalObjects(UUID collectionUuid, List<DigitalObject> digitalObjects);

  PageResponse<Collection> findActive(PageRequest pageRequest);

  SearchPageResponse<Collection> findActive(SearchPageRequest searchPageRequest);

  Collection getActive(UUID uuid);

  Collection getActive(UUID uuid, Locale pLocale);

  List<Collection> getActiveChildren(UUID uuid);

  PageResponse<Collection> getActiveChildren(UUID uuid, PageRequest pageRequest);

  default SearchPageResponse<DigitalObject> getDigitalObjects(
      Collection collection, SearchPageRequest searchPageRequest) {
    if (collection == null) {
      return null;
    }
    return getDigitalObjects(collection.getUuid(), searchPageRequest);
  }

  SearchPageResponse<DigitalObject> getDigitalObjects(UUID collectionUuid, SearchPageRequest searchPageRequest);

  List<CorporateBody> getRelatedCorporateBodies(UUID uuid, Filtering filtering);

  default boolean removeDigitalObject(Collection collection, DigitalObject digitalObject) {
    if (collection == null || digitalObject == null) {
      return false;
    }
    return removeDigitalObject(collection.getUuid(), digitalObject.getUuid());
  }

  boolean removeDigitalObject(UUID collectionUuid, UUID digitalObjectUuid);

  /**
   * Removes a digitalObject from all collections, to which it was connected to.
   *
   * @param digitalObject the digital object
   * @return boolean value for success
   */
  boolean removeDigitalObjectFromAllCollections(DigitalObject digitalObject);

  default boolean saveDigitalObjects(Collection collection, List<DigitalObject> digitalObjects) {
    if (collection == null || digitalObjects == null) {
      return false;
    }
    return saveDigitalObjects(collection.getUuid(), digitalObjects);
  }

  boolean saveDigitalObjects(UUID collectionUuid, List<DigitalObject> digitalObjects);
}
