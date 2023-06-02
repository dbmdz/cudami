package de.digitalcollections.cudami.server.controller.identifiable.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.agent.GivenNameService;
import de.digitalcollections.cudami.server.controller.CudamiControllerException;
import de.digitalcollections.cudami.server.controller.legacy.V5MigrationHelper;
import de.digitalcollections.cudami.server.controller.legacy.model.LegacyPageRequest;
import de.digitalcollections.model.identifiable.agent.GivenName;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "V5 Given name controller")
public class V5GivenNameController {

  private final GivenNameService givenNameService;

  private final ObjectMapper objectMapper;

  public V5GivenNameController(GivenNameService givenNameService, ObjectMapper objectMapper) {
    this.givenNameService = givenNameService;
    this.objectMapper = objectMapper;
  }

  @Operation(summary = "get all given names")
  @GetMapping(
      value = {"/v5/givennames"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "language", required = false, defaultValue = "de") String language,
      @RequestParam(name = "searchTerm", required = false) String searchTerm,
      @RequestParam(name = "initial", required = false) String initial)
      throws CudamiControllerException, ServiceException {
    PageRequest pageRequest = new LegacyPageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }

    PageResponse<GivenName> pageResponse;
    if (initial == null) {
      pageResponse = givenNameService.find(pageRequest);
    } else {
      pageResponse = givenNameService.findByLanguageAndInitial(pageRequest, language, initial);
    }
    try {
      String result = V5MigrationHelper.migrate(pageResponse, objectMapper);
      return new ResponseEntity<>(result, HttpStatus.OK);
    } catch (JsonProcessingException e) {
      throw new CudamiControllerException(e);
    }
  }
}
