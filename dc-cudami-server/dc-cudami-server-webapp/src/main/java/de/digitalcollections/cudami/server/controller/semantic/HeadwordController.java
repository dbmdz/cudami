package de.digitalcollections.cudami.server.controller.semantic;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.semantic.HeadwordService;
import de.digitalcollections.model.filter.FilterCriterion;
import de.digitalcollections.model.filter.FilterOperation;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.Sorting;
import de.digitalcollections.model.semantic.Headword;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Headword controller")
public class HeadwordController {

  private final HeadwordService headwordService;

  public HeadwordController(HeadwordService headwordService) {
    this.headwordService = headwordService;
  }

  @Operation(summary = "Get count of headwords")
  @GetMapping(
      value = {"/v5/headwords/count"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public long count() {
    return headwordService.count();
  }

  @Operation(summary = "Delete an headword with all its relations")
  @DeleteMapping(
      value = {"/v5/headwords/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity delete(
      @Parameter(example = "", description = "UUID of the headword") @PathVariable("uuid")
          UUID uuid) {
    boolean successful = headwordService.delete(uuid);

    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }
    return new ResponseEntity<>(successful, HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Get all headwords as (filtered, sorted, paged) list")
  @GetMapping(
      value = {"/v5/headwords"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Headword> findAll(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "label", required = false) String label,
      @RequestParam(name = "language", required = false) String language) {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    if (label != null || language != null) {
      Filtering filtering = new Filtering();
      if (label != null) {
        filtering.add(
            Filtering.defaultBuilder()
                .add(new FilterCriterion<String>("label", FilterOperation.EQUALS, label))
                .build());
      }
      if (label != null) {
        filtering.add(
            Filtering.defaultBuilder()
                .add(
                    new FilterCriterion<Locale>(
                        "locale", FilterOperation.EQUALS, Locale.forLanguageTag(language)))
                .build());
      }
      pageRequest.setFiltering(filtering);
    }
    return headwordService.find(pageRequest);
  }

  @Operation(summary = "Get an headword by uuid")
  @GetMapping(
      value = {
        "/v5/headwords/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Headword findByUuid(@PathVariable UUID uuid) {
    return headwordService.get(uuid);
  }

  @Operation(summary = "Find limited amount of random headwords")
  @GetMapping(
      value = {"/v5/headwords/random"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Headword> findRandom(
      @RequestParam(name = "count", required = false, defaultValue = "5") int count) {
    return headwordService.getRandom(count);
  }

  @Operation(summary = "Get related file resources of an headword")
  @GetMapping(
      value = {"/v5/headwords/{uuid}/related/fileresources"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<FileResource> getRelatedFileResources(
      @Parameter(example = "", description = "UUID of the headword") @PathVariable("uuid")
          UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize) {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
    return headwordService.getRelatedFileResources(uuid, pageRequest);
  }

  @Operation(summary = "Get related entities of an headword")
  @GetMapping(
      value = {"/v5/headwords/{uuid}/related/entities"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Entity> getRelatedEntities(
      @Parameter(example = "", description = "UUID of the headword") @PathVariable("uuid")
          UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize) {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
    return headwordService.getRelatedEntities(uuid, pageRequest);
  }

  @Operation(summary = "Save a newly created headword")
  @PostMapping(
      value = {"/v5/headwords"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Headword save(@RequestBody Headword headword, BindingResult errors)
      throws IdentifiableServiceException, ServiceException {
    return headwordService.save(headword);
  }

  @Operation(summary = "Save list of related entities for a given headword")
  @PostMapping(
      value = {"/v5/headwords/{uuid}/entities"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Entity> saveRelatedEntities(
      @Parameter(example = "", description = "UUID of the headword") @PathVariable("uuid")
          UUID uuid,
      @RequestBody List<Entity> entities) {
    return headwordService.saveRelatedEntities(uuid, entities);
  }

  @Operation(summary = "Save list of related fileresources for a given headword")
  @PostMapping(
      value = {"/v5/headwords/{uuid}/fileresources"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<FileResource> saveRelatedFileResources(
      @Parameter(example = "", description = "UUID of the headword") @PathVariable("uuid")
          UUID uuid,
      @RequestBody List<FileResource> fileResources) {
    return headwordService.saveRelatedFileResources(uuid, fileResources);
  }

  @Operation(summary = "Update an headword")
  @PutMapping(
      value = {"/v5/headwords/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Headword update(
      @Parameter(example = "", description = "UUID of the headword") @PathVariable("uuid")
          UUID uuid,
      @RequestBody Headword headword,
      BindingResult errors)
      throws IdentifiableServiceException, ServiceException {
    assert Objects.equals(uuid, headword.getUuid());
    return headwordService.update(headword);
  }
}
