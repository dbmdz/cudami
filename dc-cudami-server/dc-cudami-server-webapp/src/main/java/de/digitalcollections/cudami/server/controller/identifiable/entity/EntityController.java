package de.digitalcollections.cudami.server.controller.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.EntityService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.relation.EntityToEntityRelationService;
import de.digitalcollections.cudami.server.controller.ParameterHelper;
import de.digitalcollections.cudami.server.controller.identifiable.AbstractIdentifiableController;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Entity controller")
public class EntityController<E extends Entity> extends AbstractIdentifiableController<Entity> {

  private final EntityToEntityRelationService entityRelationService;
  private final EntityService<Entity> service;

  public EntityController(
      EntityToEntityRelationService entityRelationService,
      @Qualifier("entityService") EntityService<Entity> entityService) {
    this.entityRelationService = entityRelationService;
    this.service = entityService;
  }

  @Operation(summary = "Get count of entities")
  @GetMapping(
      value = {
        "/v6/entities/count",
        "/v5/entities/count",
        "/v3/entities/count",
        "/latest/entities/count"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public long count() {
    return service.count();
  }

  @Operation(summary = "Get all entities as (paged, sorted, filtered) list")
  @GetMapping(
      value = {"/v6/entities"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Entity> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "filter", required = false) List<FilterCriterion> filterCriteria) {
    PageRequest pageRequest =
        createPageRequest(Entity.class, pageNumber, pageSize, sortBy, filterCriteria);
    return service.find(pageRequest);
  }

  @Override
  @Operation(
      summary = "Get an entity by namespace and id",
      description =
          "Separate namespace and id with a colon, e.g. foo:bar. It is also possible, to add a .json suffix, which will be ignored then")
  @GetMapping(
      value = {
        "/v6/entities/identifier/**",
        "/v5/entities/identifier/**",
        "/latest/entities/identifier/**"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Entity> getByIdentifier(HttpServletRequest request)
      throws ServiceException, ValidationException {
    Pair<String, String> namespaceAndId =
        ParameterHelper.extractPairOfStringsFromUri(request.getRequestURI(), "^.*?/identifier/");
    if (namespaceAndId.getLeft().isBlank()
        || (namespaceAndId.getRight() == null || namespaceAndId.getRight().isBlank())) {
      throw new ValidationException(
          "No namespace and/or id were provided in a colon separated manner");
    }

    Entity entity = service.getByIdentifier(namespaceAndId.getLeft(), namespaceAndId.getRight());
    return new ResponseEntity<>(entity, entity != null ? HttpStatus.OK : HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Get entity by reference id")
  @GetMapping(
      value = {
        "/v6/entities/{refId:[0-9]+}",
        "/v5/entities/{refId:[0-9]+}",
        "/latest/entities/{refId:[0-9]+}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Entity> getByRefId(@PathVariable long refId) {
    Entity entity = service.getByRefId(refId);
    return new ResponseEntity<>(entity, entity != null ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    // routing should be done in frontend webapp (second call will be sent on entity type)
    //    EntityType entityType = entity.getEntityType();
    //    UUID uuid = entity.getUuid();
    //    switch (entityType) {
    //      case PERSON:
    //        return personService.getByIdentifier(uuid);
    //    }
  }

  @Operation(summary = "Get entity by uuid")
  @GetMapping(
      value = {
        "/v6/entities/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/entities/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v2/entities/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/latest/entities/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Entity> getByUuid(@PathVariable UUID uuid) throws ServiceException {
    Entity entity = service.getByUuid(uuid);
    return new ResponseEntity<>(entity, entity != null ? HttpStatus.OK : HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Find limited amount of random entites")
  @GetMapping(
      value = {
        "/v6/entities/random",
        "/v5/entities/random",
        "/v2/entities/random",
        "/latest/entities/random"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Entity> getRandomEntities(
      @RequestParam(name = "count", required = false, defaultValue = "5") int count) {
    return service.getRandom(count);
  }

  @Operation(summary = "Get related file resources of entity")
  @GetMapping(
      value = {
        "/v6/entities/{uuid:" + ParameterHelper.UUID_PATTERN + "}/related/fileresources",
        "/v5/entities/{uuid:" + ParameterHelper.UUID_PATTERN + "}/related/fileresources",
        "/v2/entities/{uuid:" + ParameterHelper.UUID_PATTERN + "}/related/fileresources",
        "/latest/entities/{uuid:" + ParameterHelper.UUID_PATTERN + "}/related/fileresources"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<FileResource> getRelatedFileResources(@PathVariable UUID uuid) {
    return service.getRelatedFileResources(uuid);
  }

  @Operation(summary = "Get relations for an entity (being the subject)")
  @GetMapping(
      value = {
        "/v6/entities/relations/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/entities/relations/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v2/entities/relations/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/latest/entities/relations/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<EntityRelation> getRelations(@PathVariable UUID uuid) {
    return entityRelationService.getBySubject(uuid);
  }

  @Override
  protected IdentifiableService<Entity> getService() {
    return service;
  }
}
