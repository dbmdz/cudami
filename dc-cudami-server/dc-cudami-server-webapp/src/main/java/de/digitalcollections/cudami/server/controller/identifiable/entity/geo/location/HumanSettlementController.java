package de.digitalcollections.cudami.server.controller.identifiable.entity.geo.location;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.geo.location.HumanSettlementService;
import de.digitalcollections.model.identifiable.entity.geo.location.HumanSettlement;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(description = "The human settlement controller", name = "Human settlement controller")
public class HumanSettlementController {

  private static final Logger LOGGER = LoggerFactory.getLogger(HumanSettlementController.class);

  private final HumanSettlementService humanSettlementService;

  public HumanSettlementController(HumanSettlementService humanSettlementService) {
    this.humanSettlementService = humanSettlementService;
  }

  @Operation(summary = "get all human settlements")
  @GetMapping(
      value = {"/latest/human_settlements", "/v2/human_settlements"},
      produces = "application/json")
  public PageResponse<HumanSettlement> findAll(
      Pageable pageable,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "language", required = false, defaultValue = "de") String language,
      @RequestParam(name = "initial", required = false) String initial) {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    if (initial == null) {
      return humanSettlementService.find(pageRequest);
    }
    return humanSettlementService.findByLanguageAndInitial(pageRequest, language, initial);
  }

  @Operation(
      summary =
          "get a human settlement as JSON or XML, depending on extension or <tt>format</tt> request parameter or accept header")
  @GetMapping(
      value = {
        "/latest/human_settlements/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}",
        "/v2/human_settlements/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}"
      },
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  public ResponseEntity<HumanSettlement> get(
      @Parameter(
              example = "",
              description =
                  "UUID of the human settlement, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @Parameter(
              name = "pLocale",
              description =
                  "Desired locale, e.g. <tt>de</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false)
          Locale pLocale)
      throws IdentifiableServiceException {

    HumanSettlement result;
    if (pLocale == null) {
      result = humanSettlementService.get(uuid);
    } else {
      result = humanSettlementService.get(uuid, pLocale);
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @Operation(
      summary =
          "get a human settlement as JSON or XML, depending on extension or <tt>format</tt> request parameter or accept header")
  @GetMapping(
      value = {"/latest/human_settlements/identifier", "/v2/human_settlements/identifier"},
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  public ResponseEntity<HumanSettlement> getByIdentifier(
      @RequestParam(name = "namespace", required = true) String namespace,
      @RequestParam(name = "id", required = true) String id)
      throws IdentifiableServiceException {
    HumanSettlement result = humanSettlementService.getByIdentifier(namespace, id);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @Operation(summary = "save a newly created human settlement")
  @PostMapping(
      value = {"/latest/human_settlements", "/v2/human_settlements"},
      produces = "application/json")
  public HumanSettlement save(@RequestBody HumanSettlement humanSettlement, BindingResult errors)
      throws IdentifiableServiceException {
    return humanSettlementService.save(humanSettlement);
  }

  @Operation(summary = "update a human settlement")
  @PutMapping(
      value = {"/latest/human_settlements/{uuid}", "/v2/human_settlements/{uuid}"},
      produces = "application/json")
  public HumanSettlement update(
      @PathVariable("uuid") UUID uuid,
      @RequestBody HumanSettlement humanSettlement,
      BindingResult errors)
      throws IdentifiableServiceException {
    assert Objects.equals(uuid, humanSettlement.getUuid());
    return humanSettlementService.update(humanSettlement);
  }
}
