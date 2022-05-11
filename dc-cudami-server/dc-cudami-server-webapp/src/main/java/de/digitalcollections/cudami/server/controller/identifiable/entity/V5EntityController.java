package de.digitalcollections.cudami.server.controller.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.EntityService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.relation.EntityRelationService;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Entity controller")
public class V5EntityController<E extends Entity> {

  private final EntityRelationService entityRelationService;
  private final EntityService<Entity> entityService;

  public V5EntityController(
      EntityRelationService entityRelationService,
      @Qualifier("entityService") EntityService<Entity> entityService) {
    this.entityRelationService = entityRelationService;
    this.entityService = entityService;
  }

  @Operation(
      summary = "Find limited amount of entities containing searchTerm in label or description")
  @GetMapping(
      value = {"/v5/entities/search", "/v2/entities/search", "/latest/entities/search"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm,
      @RequestParam(name = "entityType", required = false)
          FilterCriterion<String> entityTypeCriterion) {
    PageRequest pageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    if (entityTypeCriterion != null) {
      Filtering filtering = Filtering.builder().add("entityType", entityTypeCriterion).build();
      pageRequest.setFiltering(filtering);
    }
    PageResponse<Entity> response = entityService.find(pageRequest);
    // TODO
    return null;
  }
}
