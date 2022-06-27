package de.digitalcollections.cudami.server.controller.identifiable.resource;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.ImageFileResourceService;
import de.digitalcollections.cudami.server.controller.identifiable.AbstractIdentifiableController;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Image Fileresource controller")
public class ImageFileResourceController extends AbstractIdentifiableController<ImageFileResource> {

  private final ImageFileResourceService service;

  public ImageFileResourceController(ImageFileResourceService service) {
    this.service = service;
  }

  @Override
  protected IdentifiableService<ImageFileResource> getService() {
    return service;
  }

  @Operation(summary = "Get a paged and filtered list of ImageFileResources")
  @GetMapping(
      value = {"/v6/imagefileresources", "/v6/imagefileresources/search"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<ImageFileResource> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "filename", required = false)
          FilterCriterion<String> filenameFilterCriterion) {

    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    if (filenameFilterCriterion != null) {
      FilterCriterion<String> filename =
          new FilterCriterion<>(
              "filename",
              filenameFilterCriterion.getOperation(),
              URLDecoder.decode(
                  (String) filenameFilterCriterion.getValue(), StandardCharsets.UTF_8));
      Filtering filtering = Filtering.builder().add("filename", filename).build();
      pageRequest.setFiltering(filtering);
    }

    return service.find(pageRequest);
  }

  @Operation(
      summary = "Get an ImageFileResource by namespace and id",
      description =
          "Separate namespace and id with a colon, e.g. foo:bar. It is also possible, to add a .json suffix, which will be ignored then")
  @GetMapping(
      value = {"/v6/imagefileresources/identifier/**"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ImageFileResource> getByIdentifier(HttpServletRequest request)
      throws IdentifiableServiceException, ValidationException {
    return super.getByIdentifier(request);
  }

  @Operation(summary = "Get an ImageFileResource by uuid")
  @GetMapping(
      value = {"/v6/imagefileresources/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ImageFileResource> getByUuid(
      @Parameter(
              example = "",
              description =
                  "UUID of the ImageFileResource, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @Parameter(
              name = "pLocale",
              description =
                  "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false)
          Locale pLocale)
      throws IdentifiableServiceException {
    ImageFileResource imageFileResource;
    if (pLocale == null) {
      imageFileResource = service.getByUuid(uuid);
    } else {
      imageFileResource = service.getByUuidAndLocale(uuid, pLocale);
    }
    return new ResponseEntity<>(imageFileResource, HttpStatus.OK);
  }

  @Operation(summary = "Save a newly created ImageFileResource")
  @PostMapping(
      value = {"/v6/imagefileresources"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ImageFileResource save(@RequestBody ImageFileResource imageFileResource)
      throws IdentifiableServiceException, ValidationException {
    return service.save(imageFileResource);
  }

  @Operation(summary = "Update an ImageFileResource")
  @PutMapping(
      value = {"/v6/imagefileresources/{uuid}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ImageFileResource update(
      @PathVariable UUID uuid, @RequestBody ImageFileResource imageFileResource)
      throws IdentifiableServiceException, ValidationException {
    assert Objects.equals(uuid, imageFileResource.getUuid());
    return service.update(imageFileResource);
  }
}
