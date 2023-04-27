package de.digitalcollections.cudami.server.controller.legal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.legal.LicenseService;
import de.digitalcollections.cudami.server.controller.CudamiControllerException;
import de.digitalcollections.cudami.server.controller.legacy.V5MigrationHelper;
import de.digitalcollections.model.legal.License;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "V5 License controller")
public class V5LicenseController {

  private final LicenseService service;
  private final ObjectMapper objectMapper;

  public V5LicenseController(LicenseService service, ObjectMapper objectMapper) {
    this.service = service;
    this.objectMapper = objectMapper;
  }

  @Operation(summary = "Get all licenses as (filtered, sorted, paged) list")
  @GetMapping(
      value = {"/v5/licenses"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "label", required = false) FilterCriterion<String> labelCriterion,
      @RequestParam(name = "locale", required = false) FilterCriterion<String> localeCriterion)
      throws CudamiControllerException, ServiceException {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    if (labelCriterion != null || localeCriterion != null) {
      Filtering filtering = new Filtering();
      if (labelCriterion != null) {
        filtering.add(Filtering.builder().add("label", labelCriterion).build());
      }
      if (localeCriterion != null) {
        filtering.add(
            Filtering.builder()
                .add(
                    new FilterCriterion<Locale>(
                        "locale",
                        localeCriterion.getOperation(),
                        Locale.forLanguageTag(localeCriterion.getValue().toString())))
                .build());
      }
      pageRequest.setFiltering(filtering);
    }

    PageResponse<License> pageResponse = service.find(pageRequest);

    try {
      String result = V5MigrationHelper.migrate(pageResponse, objectMapper);
      return new ResponseEntity<>(result, HttpStatus.OK);
    } catch (JsonProcessingException e) {
      throw new CudamiControllerException(e);
    }
  }
}
