package de.digitalcollections.cudami.server.controller.identifiable.resource;

import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.LinkedDataFileResourceService;
import de.digitalcollections.model.identifiable.resource.LinkedDataFileResource;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "LinkedData Fileresource controller")
public class V5LinkedDataFileResourceController {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(V5LinkedDataFileResourceController.class);

  private final LinkedDataFileResourceService service;

  public V5LinkedDataFileResourceController(LinkedDataFileResourceService service) {
    this.service = service;
  }

  @Operation(summary = "Find a limited and filtered amount of LinkedDataFileResources")
  @GetMapping(
      value = {"/v5/linkeddatafileresources/search"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm,
      @RequestParam(name = "uri", required = false)
          FilterCriterion<String> encodedUriFilterCriterion) {
    PageRequest searchPageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      searchPageRequest.setSorting(sorting);
    }
    if (encodedUriFilterCriterion != null) {
      FilterCriterion<String> uri =
          new FilterCriterion<>(
              "uri",
              encodedUriFilterCriterion.getOperation(),
              URLDecoder.decode(
                  (String) encodedUriFilterCriterion.getValue(), StandardCharsets.UTF_8));
      Filtering filtering = Filtering.builder().add("uri", uri).build();
      searchPageRequest.setFiltering(filtering);
    }

    PageResponse<LinkedDataFileResource> response = service.find(searchPageRequest);
    // TODO
    return null;
  }
}
