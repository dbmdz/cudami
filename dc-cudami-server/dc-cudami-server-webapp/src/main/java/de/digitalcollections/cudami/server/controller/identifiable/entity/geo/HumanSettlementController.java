package de.digitalcollections.cudami.server.controller.identifiable.entity.geo;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.geo.HumanSettlementService;
import de.digitalcollections.model.api.identifiable.entity.geo.HumanSettlement;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.Sorting;
import de.digitalcollections.model.api.paging.enums.Direction;
import de.digitalcollections.model.api.paging.enums.NullHandling;
import de.digitalcollections.model.impl.paging.OrderImpl;
import de.digitalcollections.model.impl.paging.PageRequestImpl;
import de.digitalcollections.model.impl.paging.SortingImpl;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiPathParam;
import org.jsondoc.core.annotation.ApiQueryParam;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
@Api(description = "The human settlement controller", name = "Human settlement controller")
public class HumanSettlementController {

  private static final Logger LOGGER = LoggerFactory.getLogger(HumanSettlementController.class);

  @Autowired HumanSettlementService humanSettlementService;

  @ApiMethod(description = "get all human settlements")
  @GetMapping(
      value = {"/latest/human_settlements", "/v2/human_settlements"},
      produces = "application/json")
  @ApiResponseObject
  public PageResponse<HumanSettlement> findAll(
      Pageable pageable,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortField", required = false, defaultValue = "uuid") String sortField,
      @RequestParam(name = "sortDirection", required = false, defaultValue = "ASC")
          Direction sortDirection,
      @RequestParam(name = "nullHandling", required = false, defaultValue = "NATIVE")
          NullHandling nullHandling,
      @RequestParam(name = "language", required = false, defaultValue = "de") String language,
      @RequestParam(name = "initial", required = false) String initial) {
    OrderImpl order = new OrderImpl(sortDirection, sortField, nullHandling);
    Sorting sorting = new SortingImpl(order);
    PageRequest pageRequest = new PageRequestImpl(pageNumber, pageSize, sorting);
    if (initial == null) {
      return humanSettlementService.find(pageRequest);
    }
    return humanSettlementService.findByLanguageAndInitial(pageRequest, language, initial);
  }

  @ApiMethod(
      description =
          "get a human settlement as JSON or XML, depending on extension or <tt>format</tt> request parameter or accept header")
  @GetMapping(
      value = {
        "/latest/human_settlements/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}",
        "/v2/human_settlements/{uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}"
      },
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ApiResponseObject
  public ResponseEntity<HumanSettlement> get(
      @ApiPathParam(
              description =
                  "UUID of the human settlement, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @ApiQueryParam(
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

  @ApiMethod(
      description =
          "get a human settlement as JSON or XML, depending on extension or <tt>format</tt> request parameter or accept header")
  @GetMapping(
      value = {"/latest/human_settlements/identifier", "/v2/human_settlements/identifier"},
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ApiResponseObject
  public ResponseEntity<HumanSettlement> getByIdentifier(
      @RequestParam(name = "namespace", required = true) String namespace,
      @RequestParam(name = "id", required = true) String id)
      throws IdentifiableServiceException {
    HumanSettlement result = humanSettlementService.getByIdentifier(namespace, id);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @ApiMethod(description = "save a newly created human settlement")
  @PostMapping(
      value = {"/latest/human_settlements", "/v2/human_settlements"},
      produces = "application/json")
  @ApiResponseObject
  public HumanSettlement save(@RequestBody HumanSettlement humanSettlement, BindingResult errors)
      throws IdentifiableServiceException {
    return humanSettlementService.save(humanSettlement);
  }

  @ApiMethod(description = "update a human settlement")
  @PutMapping(
      value = {"/latest/human_settlements/{uuid}", "/v2/human_settlements/{uuid}"},
      produces = "application/json")
  @ApiResponseObject
  public HumanSettlement update(
      @PathVariable("uuid") UUID uuid,
      @RequestBody HumanSettlement humanSettlement,
      BindingResult errors)
      throws IdentifiableServiceException {
    assert Objects.equals(uuid, humanSettlement.getUuid());
    return humanSettlementService.update(humanSettlement);
  }
}
