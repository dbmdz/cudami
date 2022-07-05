package de.digitalcollections.cudami.server.controller.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.EntityService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.relation.EntityRelationService;
import de.digitalcollections.cudami.server.controller.ParameterHelper;
import de.digitalcollections.cudami.server.controller.identifiable.AbstractIdentifiableController;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

  private final EntityRelationService entityRelationService;
  private final EntityService<Entity> entityService;

  public EntityController(
      EntityRelationService entityRelationService,
      @Qualifier("entityService") EntityService<Entity> entityService) {
    this.entityRelationService = entityRelationService;
    this.entityService = entityService;
  }

  @Override
  protected IdentifiableService<Entity> getService() {
    return entityService;
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
    return entityService.count();
  }

  @Operation(summary = "Get all entities")
  @GetMapping(
      value = {"/v6/entities"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Entity> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm,
      @RequestParam(name = "label", required = false) String labelTerm,
      @RequestParam(name = "labelLanguage", required = false) Locale labelLanguage,
      @RequestParam(name = "entityType", required = false)
          FilterCriterion<String> entityTypeCriterion) {
    return super.find(
        pageNumber,
        pageSize,
        sortBy,
        searchTerm,
        labelTerm,
        labelLanguage,
        Map.of("entityType", entityTypeCriterion));
  }

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
      throws IdentifiableServiceException, ValidationException {
    Pair<String, String> namespaceAndId =
        ParameterHelper.extractPairOfStringsFromUri(request.getRequestURI(), "^.*?/identifier/");
    if (namespaceAndId.getLeft().isBlank()
        || (namespaceAndId.getRight() == null || namespaceAndId.getRight().isBlank())) {
      throw new ValidationException(
          "No namespace and/or id were provided in a colon separated manner");
    }

    Entity entity =
        entityService.getByIdentifier(namespaceAndId.getLeft(), namespaceAndId.getRight());
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
        "/v6/entities/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}",
        "/v5/entities/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}",
        "/v2/entities/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}",
        "/latest/entities/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Entity getByUuid(@PathVariable UUID uuid) {
    return entityService.getByUuid(uuid);
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
    return entityService.getRandom(count);
  }

  @Operation(summary = "Get related file resources of entity")
  @GetMapping(
      value = {
        "/v6/entities/{uuid}/related/fileresources",
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
        "/v6/entities/relations/{uuid}",
        "/v5/entities/relations/{uuid}",
        "/v2/entities/relations/{uuid}",
        "/latest/entities/relations/{uuid}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  List<EntityRelation> getRelations(@PathVariable UUID uuid) {
    return entityRelationService.getBySubject(uuid);
  }
}
