package de.digitalcollections.cudami.server.controller.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.DigitalObjectService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.EntityRelationsService;
import de.digitalcollections.model.api.identifiable.entity.EntityRelation;
import java.util.List;
import java.util.UUID;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiPathParam;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(description = "The entity relations controller", name = "Entity relations controller")
public class EntityRelationsController {

  @Autowired private DigitalObjectService digitalObjectService;

  @Autowired private EntityRelationsService service;

  @ApiMethod(
      description =
          "Connect a list of two entities, which share the same subject; obsolete; please use generic method without subjectuuid in path instead.")
  @PutMapping(
      value = {"/latest/entities/{subjectuuid}/relations", "/v3/entities/{subjectuuid}/relations"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Deprecated
  @ApiResponseObject
  List<EntityRelation> saveEntityRelationsForSubject(
      @PathVariable("subjectuuid") UUID subjectUuid,
      @RequestBody List<EntityRelation> entityRelations) {
    if (!subjectUuid.equals(entityRelations.get(0).getSubject().getUuid())) {
      throw new IllegalArgumentException(
          "Mismatching arguments. SubjectUuid must match the Uuid of the subject of the first item");
    }
    return digitalObjectService.saveRelations(entityRelations);
  }

  @ApiMethod(description = "Connect a list of entity pairs with a predicate each")
  @PutMapping(
      value = {"/latest/entities/relations", "/v3/entities/relations"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  List<EntityRelation> saveEntityRelations(@RequestBody List<EntityRelation> entityRelations) {
    return service.saveEntityRelations(entityRelations);
  }

  @ApiMethod(description = "Delete all entity relations for a given subject and predicate")
  @DeleteMapping(
      value = {
        "/latest/entities/relations/{subjectuuid}/{predicate}",
        "/v3/entities/relations/{subjectuuid}/{predicate}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponseObject
  public ResponseEntity deleteAllEntityRelationsForSubjectAndPredicate(
      @ApiPathParam(description = "UUID of the subject") @PathVariable("subjectuuid")
          UUID subjectUuid,
      @ApiPathParam(description = "predicate") @PathVariable("predicate") String predicate) {

    boolean successful = service.deleteAllForSubjectAndPredicate(subjectUuid, predicate);

    if (successful) {
      return new ResponseEntity<>(successful, HttpStatus.OK);
    }

    // Errors can only happen for a bad request, when one of the two required params is empty
    return new ResponseEntity<>(successful, HttpStatus.BAD_REQUEST);
  }
}
