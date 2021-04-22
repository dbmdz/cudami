package de.digitalcollections.cudami.server.controller.identifiable.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.DigitalObjectService;
import de.digitalcollections.cudami.server.controller.AbstractLegacyController;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.Sorting;
import java.util.UUID;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiPathParam;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(description = "The digital object controller V3", name = "Digital object controller V3")
public class V3DigitalObjectController extends AbstractLegacyController {

  private final DigitalObjectService digitalObjectService;

  public V3DigitalObjectController(DigitalObjectService digitalObjectService) {
    this.digitalObjectService = digitalObjectService;
  }

  @ApiMethod(description = "Get (active) paged collections of a digital objects")
  @GetMapping(
      value = {"/v3/digitalobjects/{uuid}/collections"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public ResponseEntity<String> getCollections(
      @ApiPathParam(description = "UUID of the digital object") @PathVariable("uuid") UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "active", required = false) String active)
      throws JsonProcessingException {
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize, new Sorting());

    DigitalObject digitalObject = new DigitalObject();
    digitalObject.setUuid(uuid);

    PageResponse<Collection> response;

    if (active != null) {
      response = digitalObjectService.getActiveCollections(digitalObject, pageRequest);
    } else {
      response = digitalObjectService.getCollections(digitalObject, pageRequest);
    }

    return new ResponseEntity<>(fixPageResponse(response), HttpStatus.OK);
  }
}
