package de.digitalcollections.cudami.admin.controller.identifiable;

import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.CudamiIdentifierTypesClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.IdentifierType;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/** Controller for identifier type management pages. */
@Controller
public class IdentifierTypeController extends AbstractController {

  private static final Logger LOGGER = LoggerFactory.getLogger(IdentifierTypeController.class);

  private final CudamiIdentifierTypesClient service;

  public IdentifierTypeController(CudamiClient client) {
    this.service = client.forIdentifierTypes();
  }

  @ModelAttribute("menu")
  protected String module() {
    return "identifiertypes";
  }

  @GetMapping("/identifiertypes/new")
  public String create() {
    return "identifiertypes/create";
  }

  @GetMapping("/api/identifiertypes/new")
  @ResponseBody
  public IdentifierType createModel() {
    return service.create();
  }

  @GetMapping("/identifiertypes/{uuid}/edit")
  public String edit(@PathVariable UUID uuid, Model model) throws TechnicalException {
    IdentifierType identifierType = service.getByUuid(uuid);
    model.addAttribute("label", identifierType.getLabel());
    model.addAttribute("uuid", identifierType.getUuid());
    return "identifiertypes/edit";
  }

  @GetMapping("/api/identifiertypes")
  @ResponseBody
  public PageResponse<IdentifierType> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy)
      throws TechnicalException {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    return service.find(pageRequest);
  }

  @GetMapping("/api/identifiertypes/{uuid}")
  @ResponseBody
  public IdentifierType getByUuid(@PathVariable UUID uuid) throws TechnicalException {
    return service.getByUuid(uuid);
  }

  @GetMapping("/identifiertypes")
  public String list() {
    return "identifiertypes/list";
  }

  @PostMapping("/api/identifiertypes")
  public ResponseEntity save(@RequestBody IdentifierType identifierType) {
    try {
      IdentifierType identifierTypeDb = service.save(identifierType);
      return ResponseEntity.status(HttpStatus.CREATED).body(identifierTypeDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot save identifier type: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @PutMapping("/api/identifiertypes/{uuid}")
  public ResponseEntity update(
      @PathVariable UUID uuid, @RequestBody IdentifierType identifierType) {
    try {
      IdentifierType identifierTypeDb = service.update(uuid, identifierType);
      return ResponseEntity.ok(identifierTypeDb);
    } catch (TechnicalException e) {
      LOGGER.error("Cannot update identifier type with uuid={}", uuid, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }
}
