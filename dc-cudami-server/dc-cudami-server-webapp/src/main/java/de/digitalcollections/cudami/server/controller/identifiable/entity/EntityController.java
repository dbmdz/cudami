package de.digitalcollections.cudami.server.controller.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.EntityService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.relation.EntityRelationService;
import de.digitalcollections.model.filter.FilterCriterion;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.paging.Sorting;
import java.util.List;
import java.util.UUID;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(description = "The entity controller", name = "Entity controller")
public class EntityController<E extends Entity> {

  @Autowired
  @Qualifier("entityServiceImpl")
  private EntityService<Entity> service;

  @Autowired private EntityRelationService entityRelationService;

  @ApiMethod(description = "Get count of entities")
  @GetMapping(
      value = {"/latest/entities/count", "/v3/entities/count"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public long count() {
    return service.count();
  }

  @ApiMethod(description = "Get all entities")
  @GetMapping(
      value = {"/latest/entities", "/v3/entities"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public PageResponse<Entity> findAll(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "entityType", required = false)
          FilterCriterion<String> entityTypeCriterion) {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    if (entityTypeCriterion != null) {
      Filtering filtering =
          Filtering.defaultBuilder().add("entityType", entityTypeCriterion).build();
      pageRequest.setFiltering(filtering);
    }
    return service.find(pageRequest);
  }

  @ApiMethod(
      description = "Find limited amount of entities containing searchTerm in label or description")
  @GetMapping(
      value = {"/latest/entities/search", "/v2/entities/search"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public SearchPageResponse<Entity> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm,
      @RequestParam(name = "entityType", required = false)
          FilterCriterion<String> entityTypeCriterion) {
    SearchPageRequest pageRequest = new SearchPageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    if (entityTypeCriterion != null) {
      Filtering filtering =
          Filtering.defaultBuilder().add("entityType", entityTypeCriterion).build();
      pageRequest.setFiltering(filtering);
    }
    return service.find(pageRequest);
  }

  @ApiMethod(description = "Get entity by uuid")
  @GetMapping(
      value = {
        "/latest/entities/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}",
        "/v2/entities/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public Entity findById(@PathVariable UUID uuid) {
    return service.get(uuid);
  }

  @ApiMethod(description = "Get entity by namespace and id")
  @GetMapping(
      value = {"/latest/entities/identifier/{namespace}:{id}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public Entity findByIdentifier(@PathVariable String namespace, @PathVariable String id)
      throws IdentifiableServiceException {
    Entity entity = service.getByIdentifier(namespace, id);
    return entity;
  }

  @ApiMethod(description = "Get entity by reference id")
  @GetMapping(
      value = {"/latest/entities/{refId:[0-9]+}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public Entity findByRefId(@PathVariable long refId) {
    Entity entity = service.getByRefId(refId);
    if (entity == null) {
      return null;
    }
    // routing should be done in frontend webapp (second call will be sent on entity type)
    //    EntityType entityType = entity.getEntityType();
    //    UUID uuid = entity.getUuid();
    //    switch (entityType) {
    //      case PERSON:
    //        return personService.get(uuid);
    //    }
    return entity;
  }

  @ApiMethod(description = "Find limited amount of random entites")
  @GetMapping(
      value = {"/latest/entities/random", "/v2/entities/random"},
      produces = "application/json")
  @ApiResponseObject
  public List<Entity> findRandomEntities(
      @RequestParam(name = "count", required = false, defaultValue = "5") int count) {
    return service.getRandom(count);
  }

  @ApiMethod(description = "Get related file resources of entity")
  @GetMapping(
      value = {
        "/latest/entities/{uuid}/related/fileresources",
        "/v2/entities/{uuid}/related/fileresources"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  List<FileResource> getRelatedFileResources(@PathVariable UUID uuid) {
    return service.getRelatedFileResources(uuid);
  }

  @ApiMethod(description = "Get relations for an entity (being the subject)")
  @GetMapping(
      value = {"/latest/entities/relations/{uuid}", "/v2/entities/relations/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  List<EntityRelation> getRelations(@PathVariable UUID uuid) {
    return entityRelationService.getBySubject(uuid);
  }
}
