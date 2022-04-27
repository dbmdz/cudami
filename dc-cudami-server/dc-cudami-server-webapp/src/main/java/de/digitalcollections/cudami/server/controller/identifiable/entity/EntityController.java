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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Entity controller")
public class EntityController<E extends Entity> {

  private final EntityRelationService entityRelationService;
  private final EntityService<Entity> entityService;

  public EntityController(
      EntityRelationService entityRelationService,
      @Qualifier("entityService") EntityService<Entity> entityService) {
    this.entityRelationService = entityRelationService;
    this.entityService = entityService;
  }

  @Operation(summary = "Get count of entities")
  @GetMapping(
      value = {"/v5/entities/count", "/v3/entities/count", "/latest/entities/count"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public long count() {
    return entityService.count();
  }

  @Operation(
      summary = "Find limited amount of entities containing searchTerm in label or description")
  @GetMapping(
      value = {"/v5/entities/search", "/v2/entities/search", "/latest/entities/search"},
      produces = MediaType.APPLICATION_JSON_VALUE)
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
      Filtering filtering = Filtering.builder().add("entityType", entityTypeCriterion).build();
      pageRequest.setFiltering(filtering);
    }
    return entityService.find(pageRequest);
  }

  @Operation(summary = "Get all entities")
  @GetMapping(
      value = {"/v5/entities", "/v3/entities", "/latest/entities"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Entity> find(
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
      Filtering filtering = Filtering.builder().add("entityType", entityTypeCriterion).build();
      pageRequest.setFiltering(filtering);
    }
    return entityService.find(pageRequest);
  }

  @Operation(summary = "Find limited amount of random entites")
  @GetMapping(
      value = {"/v5/entities/random", "/v2/entities/random", "/latest/entities/random"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Entity> getRandomEntities(
      @RequestParam(name = "count", required = false, defaultValue = "5") int count) {
    return entityService.getRandom(count);
  }

  @Operation(summary = "Get entity by namespace and id")
  @GetMapping(
      value = {
        "/v5/entities/identifier/{namespace}:{id}",
        "/v5/entities/identifier/{namespace}:{id}.json",
        "/latest/entities/identifier/{namespace}:{id}",
        "/latest/entities/identifier/{namespace}:{id}.json"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Entity getByIdentifier(@PathVariable String namespace, @PathVariable String id)
      throws IdentifiableServiceException {
    Entity entity = entityService.getByIdentifier(namespace, id);
    return entity;
  }

  @Operation(summary = "Get entity by reference id")
  @GetMapping(
      value = {"/v5/entities/{refId:[0-9]+}", "/latest/entities/{refId:[0-9]+}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Entity getByRefId(@PathVariable long refId) {
    Entity entity = entityService.getByRefId(refId);
    if (entity == null) {
      return null;
    }
    // routing should be done in frontend webapp (second call will be sent on entity type)
    //    EntityType entityType = entity.getEntityType();
    //    UUID uuid = entity.getUuid();
    //    switch (entityType) {
    //      case PERSON:
    //        return personService.getByIdentifier(uuid);
    //    }
    return entity;
  }

  @Operation(summary = "Get entity by uuid")
  @GetMapping(
      value = {
        "/v5/entities/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}",
        "/v2/entities/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}",
        "/latest/entities/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Entity getByUuid(@PathVariable UUID uuid) {
    return entityService.getByUuid(uuid);
  }

  @Operation(summary = "Get related file resources of entity")
  @GetMapping(
      value = {
        "/v5/entities/{uuid}/related/fileresources",
        "/v2/entities/{uuid}/related/fileresources",
        "/latest/entities/{uuid}/related/fileresources"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  List<FileResource> getRelatedFileResources(@PathVariable UUID uuid) {
    return entityService.getRelatedFileResources(uuid);
  }

  @Operation(summary = "Get relations for an entity (being the subject)")
  @GetMapping(
      value = {
        "/v5/entities/relations/{uuid}",
        "/v2/entities/relations/{uuid}",
        "/latest/entities/relations/{uuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  List<EntityRelation> getRelations(@PathVariable UUID uuid) {
    return entityRelationService.getBySubject(uuid);
  }
}
