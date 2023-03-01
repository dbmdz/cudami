package de.digitalcollections.cudami.server.controller.semantic;

import com.google.common.base.Charsets;
import de.digitalcollections.cudami.server.business.api.service.UniqueObjectService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.semantic.TagService;
import de.digitalcollections.cudami.server.controller.AbstractUniqueObjectController;
import de.digitalcollections.cudami.server.controller.ParameterHelper;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.semantic.Tag;
import io.swagger.v3.oas.annotations.Operation;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.codec.binary.Base64;
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
@io.swagger.v3.oas.annotations.tags.Tag(name = "Tag controller")
public class TagController extends AbstractUniqueObjectController<Tag> {

  private final TagService service;

  public TagController(TagService service) {
    this.service = service;
  }

  @Operation(summary = "Get all tags")
  @GetMapping(
      value = {"/v6/tags"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<Tag> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm,
      @RequestParam(name = "label", required = false) String labelTerm,
      @RequestParam(name = "labelLanguage", required = false) Locale labelLanguage) {
    return super.find(pageNumber, pageSize, sortBy, searchTerm, labelTerm, labelLanguage);
  }

  @Operation(summary = "Get tag by UUID")
  @GetMapping(
      value = {"/v6/tags/{uuid:" + ParameterHelper.UUID_PATTERN + "}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Tag> getByUuid(@PathVariable UUID uuid) {
    Tag result = service.getByUuid(uuid);
    return new ResponseEntity<>(result, result != null ? HttpStatus.OK : HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Get a tag by value")
  @GetMapping(
      value = {"/v6/tags/value/{value}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Tag> getByValue(@PathVariable String value)
      throws ValidationException, ServiceException {
    // The tag can be base-64 encoded, too
    Tag tag = service.getByValue(value);
    if (tag == null) {
      tag =
          service.getByValue(
              new String(
                  Base64.decodeBase64(value.getBytes(StandardCharsets.UTF_8)), Charsets.UTF_8));
    }

    return new ResponseEntity<>(tag, tag != null ? HttpStatus.OK : HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "Save a newly created tag")
  @PostMapping(
      value = {"/v6/tags"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Tag save(@RequestBody Tag tag, BindingResult errors) {
    return service.save(tag);
  }

  @Operation(summary = "Update a tag")
  @PutMapping(
      value = {"/v6/tags/{uuid:" + ParameterHelper.UUID_PATTERN + "}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Tag update(@PathVariable UUID uuid, @RequestBody Tag tag, BindingResult errors) {
    assert Objects.equals(uuid, tag.getUuid());
    return service.update(tag);
  }

  @Override
  protected UniqueObjectService<Tag> getService() {
    return service;
  }
}
