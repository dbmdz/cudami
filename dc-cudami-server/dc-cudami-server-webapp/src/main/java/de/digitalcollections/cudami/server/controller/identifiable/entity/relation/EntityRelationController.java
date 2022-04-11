package de.digitalcollections.cudami.server.controller.identifiable.entity.relation;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.relation.EntityRelationService;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Entity relation controller")
public class EntityRelationController {

  private final EntityRelationService entityRelationService;

  public EntityRelationController(EntityRelationService entityRelationservice) {
    this.entityRelationService = entityRelationservice;
  }

  @Operation(summary = "Get paged, sorted, filtered relations")
  @GetMapping(
      value = {"/v5/entities/relations"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<EntityRelation> getEntitiesRelations(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "predicate", required = false) String predicate) {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);

    if (StringUtils.hasText(predicate)) {
      Filtering filtering =
          Filtering.defaultBuilder().filter("predicate").isEquals(predicate).build();
      pageRequest.add(filtering);
    }
    return entityRelationService.find(pageRequest);
  }

  @Operation(
      summary =
          "Connect a list of two entities, which share the same subject; obsolete; please use generic method without subjectuuid in path instead.")
  @PutMapping(
      value = {
        "/v5/entities/{subjectuuid}/relations",
        "/v3/entities/{subjectuuid}/relations",
        "/latest/entities/{subjectuuid}/relations"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Deprecated
  /**
   * @deprecated use {@link #saveEntityRelations(List)} instead}
   */
  List<EntityRelation> saveEntityRelationsForSubject(
      @PathVariable("subjectuuid") UUID subjectUuid,
      @RequestBody List<EntityRelation> entityRelations) {
    if (!subjectUuid.equals(entityRelations.get(0).getSubject().getUuid())) {
      throw new IllegalArgumentException(
          "Mismatching arguments. SubjectUuid must match the Uuid of the subject of the first item");
    }
    return entityRelationService.save(entityRelations);
  }

  @Operation(summary = "Connect a list of entity pairs with a predicate each")
  @PutMapping(
      value = {"/v5/entities/relations", "/v3/entities/relations", "/latest/entities/relations"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  List<EntityRelation> saveEntityRelations(@RequestBody List<EntityRelation> entityRelations) {
    return entityRelationService.save(entityRelations);
  }
}
