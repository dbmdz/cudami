package de.digitalcollections.cudami.server.controller.identifiable.entity.semantic;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.semantic.SubjectService;
import de.digitalcollections.cudami.server.controller.ParameterHelper;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.semantic.Subject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.tuple.Triple;
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
@Tag(name = "Subject controller")
public class SubjectController {

  private final SubjectService service;

  public SubjectController(SubjectService service) {
    this.service = service;
  }

  @Operation(summary = "Get all subjects")
  @GetMapping(
      value = {"/v6/subjects"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Subject> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm) {
    PageRequest pageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    return service.find(pageRequest);
  }

  @Operation(
      summary = "Get a subject by type, namespace and id",
      description =
          "Separate type, namespace and id with a colon, e.g. foo:bar:baz. It is also possible, to add a .json suffix, which will be ignored then")
  @GetMapping(
      value = {"/v6/subjects/identifier/**"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Subject> getByIdentifier(HttpServletRequest request)
      throws ValidationException, ServiceException {
    Triple<String, String, String> typeNamespaceId =
        ParameterHelper.extractTripleOfStringsFromUri(request.getRequestURI(), "^.*?/identifier/");
    if (typeNamespaceId.getLeft().isBlank()
        || (typeNamespaceId.getMiddle() == null || typeNamespaceId.getMiddle().isBlank())
        || (typeNamespaceId.getRight() == null || typeNamespaceId.getRight().isBlank())) {
      throw new ValidationException(
          "No type, namespace and/or ids were provided in a colon separated manner");
    }
    Subject subject =
        service.getByTypeAndIdentifier(
            typeNamespaceId.getLeft(), typeNamespaceId.getMiddle(), typeNamespaceId.getRight());
    return new ResponseEntity<>(subject, subject != null ? HttpStatus.OK : HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Get subject by UUID")
  @GetMapping(
      value = {"/v6/subjects/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Subject> getByUuid(@PathVariable UUID uuid) {
    Subject subject = service.getByUuid(uuid);
    return new ResponseEntity<>(subject, subject != null ? HttpStatus.OK : HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Save a newly created subject")
  @PostMapping(
      value = {"/v6/subjects"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Subject save(@RequestBody Subject subject, BindingResult errors) throws ServiceException {
    service.save(subject);
    return subject;
  }

  @Operation(summary = "Update a subject")
  @PutMapping(
      value = {"/v6/subjects/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Subject update(@PathVariable UUID uuid, @RequestBody Subject subject, BindingResult errors)
      throws ServiceException {
    assert Objects.equals(uuid, subject.getUuid());
    service.update(subject);
    return subject;
  }
}
