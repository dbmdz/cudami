package de.digitalcollections.cudami.server.controller.identifiable.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.DigitalObjectService;
import de.digitalcollections.cudami.server.controller.AbstractLegacyController;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiPathParam;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(description = "The digital object controller V2", name = "Digital object controller V2")
public class V2DigitalObjectController extends AbstractLegacyController {

  private final DigitalObjectService digitalObjectService;

  public V2DigitalObjectController(DigitalObjectService digitalObjectService) {
    this.digitalObjectService = digitalObjectService;
  }

  @ApiMethod(description = "Get digital object by namespace and id")
  @GetMapping(
      value = {"/v2/digitalobjects/identifier/{namespace}:{id}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public ResponseEntity<String> findByIdentifier(
      @ApiPathParam(description = "Namespace of the identifier") @PathVariable("namespace")
          String namespace,
      @ApiPathParam(description = "value of the identifier") @PathVariable("id") String id)
      throws IdentifiableServiceException, JsonProcessingException {
    DigitalObject digitalObject = digitalObjectService.getByIdentifier(namespace, id);

    if (digitalObject == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    return new ResponseEntity<>(fixSimpleObject(digitalObject, null, null), HttpStatus.OK);
  }
}
