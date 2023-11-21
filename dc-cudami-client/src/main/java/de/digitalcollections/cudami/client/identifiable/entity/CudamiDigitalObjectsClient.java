package de.digitalcollections.cudami.client.identifiable.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.entity.item.Item;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class CudamiDigitalObjectsClient extends CudamiEntitiesClient<DigitalObject> {

  public CudamiDigitalObjectsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, DigitalObject.class, mapper, API_VERSION_PREFIX + "/digitalobjects");
  }

  public PageResponse<Collection> findActiveCollections(UUID uuid, PageRequest pageRequest)
      throws TechnicalException {
    return doGetRequestForPagedObjectList(
        String.format("%s/%s/collections?active=true", baseEndpoint, uuid),
        pageRequest,
        Collection.class);
  }

  public PageResponse<Collection> findCollections(UUID uuid, PageRequest pageRequest)
      throws TechnicalException {
    return doGetRequestForPagedObjectList(
        String.format("%s/%s/collections", baseEndpoint, uuid), pageRequest, Collection.class);
  }

  public PageResponse<Project> findProjects(UUID uuid, PageRequest pageRequest)
      throws TechnicalException {
    return doGetRequestForPagedObjectList(
        String.format("%s/%s/projects", baseEndpoint, uuid), pageRequest, Project.class);
  }

  public PageResponse<DigitalObject> getAllForParent(DigitalObject parent)
      throws TechnicalException {
    if (parent == null) {
      throw new TechnicalException("Empty parent");
    }

    PageRequest pageRequest = PageRequest.builder().pageNumber(0).pageSize(10000).build();
    return getAllForParent(parent, pageRequest);
  }

  public PageResponse<DigitalObject> getAllForParent(DigitalObject parent, PageRequest pageRequest)
      throws TechnicalException {
    pageRequest.add(
        Filtering.builder()
            .add(
                FilterCriterion.builder()
                    .withExpression("parent.uuid")
                    .isEquals(parent.getUuid())
                    .build())
            .build());
    return find(pageRequest);
  }

  public DigitalObject getByIdentifierAndFillWEMI(String namespace, String id)
      throws TechnicalException {
    return getByIdentifier(namespace, id, Map.of("fill-wemi", "true"));
  }

  public DigitalObject getByUuidAndFillWEMI(UUID uuid) throws TechnicalException {
    return getByUuid(uuid, Map.of("fill-wemi", "true"));
  }

  public PageResponse<DigitalObject> getByUuidsAndFillWEMI(List<UUID> uuids)
      throws TechnicalException {
    if (uuids == null) {
      return null;
    }

    if (uuids.size() >= 30) {
      return (PageResponse<DigitalObject>)
          doPostRequestForObject(
              String.format("%s/list?fill-wemi=true", baseEndpoint), uuids, PageResponse.class);
    } else {
      return (PageResponse<DigitalObject>)
          doGetRequestForObject(
              String.format(
                  "%s/list/%s?fill-wemi=true",
                  baseEndpoint,
                  uuids.stream().map(Object::toString).collect(Collectors.joining(","))),
              PageResponse.class);
    }
  }

  public List<FileResource> getFileResources(UUID uuid) throws TechnicalException {
    return doGetRequestForObjectList(
        String.format("%s/%s/fileresources", baseEndpoint, uuid), FileResource.class);
  }

  public List<ImageFileResource> getIiifImageFileResources(UUID uuid) throws TechnicalException {
    return doGetRequestForObjectList(
        String.format("%s/%s/fileresources/images/iiif", baseEndpoint, uuid),
        ImageFileResource.class);
  }

  public List<ImageFileResource> getImageFileResources(UUID uuid) throws TechnicalException {
    return doGetRequestForObjectList(
        String.format("%s/%s/fileresources/images", baseEndpoint, uuid), ImageFileResource.class);
  }

  public Item getItem(UUID uuid) throws TechnicalException {
    return (Item)
        doGetRequestForObject(String.format("%s/%s/item", baseEndpoint, uuid), Item.class);
  }

  public List<Locale> getLanguagesOfCollections(UUID uuid) throws TechnicalException {
    return doGetRequestForObjectList(
        String.format("%s/%s/collections/languages", baseEndpoint, uuid), Locale.class);
  }

  public List<Locale> getLanguagesOfContainedDigitalObjects(UUID uuid) throws TechnicalException {
    Filtering filtering =
        Filtering.builder()
            .add(FilterCriterion.builder().withExpression("parent.uuid").isEquals(uuid).build())
            .build();
    return doGetRequestForObjectList(baseEndpoint + "/languages", Locale.class, filtering);
  }

  public List<Locale> getLanguagesOfProjects(UUID uuid) throws TechnicalException {
    return doGetRequestForObjectList(
        String.format("%s/%s/projects/languages", baseEndpoint, uuid), Locale.class);
  }

  public List<DigitalObject> getRandomDigitalObjects(int count) throws TechnicalException {
    return doGetRequestForObjectList(
        String.format("%s/random?count=%d", baseEndpoint, count), DigitalObject.class);
  }

  public List<FileResource> setFileResources(UUID uuid, List fileResources)
      throws TechnicalException {
    return doPostRequestForObjectList(
        String.format("%s/%s/fileresources", baseEndpoint, uuid),
        fileResources,
        FileResource.class);
  }
}
