package de.digitalcollections.cudami.server.controller.identifiable.resource;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.FileResourceMetadataService;
import de.digitalcollections.cudami.server.controller.ParameterHelper;
import de.digitalcollections.cudami.server.controller.identifiable.AbstractIdentifiableController;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.validation.ValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
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
@Tag(name = "Fileresource controller")
public class FileResourceMetadataController extends AbstractIdentifiableController<FileResource> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(FileResourceMetadataController.class);

  private final FileResourceMetadataService<FileResource> service;

  public FileResourceMetadataController(
      @Qualifier("fileResourceMetadataService")
          FileResourceMetadataService<FileResource> metadataService) {
    this.service = metadataService;
  }

  @Override
  @Operation(summary = "Get all fileresources as (paged, sorted, filtered) list")
  @GetMapping(
      value = {"/v6/fileresources"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<FileResource> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "filter", required = false) List<FilterCriterion> filterCriteria,
      @RequestParam(name = "filtering", required = false) Filtering filtering)
      throws ServiceException {
    return super.find(pageNumber, pageSize, sortBy, filterCriteria, filtering);
  }

  @Operation(summary = "Get all fileresources of given type as (paged, sorted, filtered) list")
  @GetMapping(
      value = {"/v6/fileresources/type/{type}"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResponse<FileResource> findByType(
      @Parameter(example = "", description = "Type of the fileresource, e.g. <tt>image</tt>")
          @PathVariable("type")
          String type,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "filter", required = false) List<FilterCriterion> filterCriteria,
      @RequestParam(name = "filtering", required = false) Filtering filtering)
      throws ServiceException {
    PageRequest pageRequest =
        createPageRequest(
            FileResource.class, pageNumber, pageSize, sortBy, filterCriteria, filtering);

    String prefix;
    switch (type) {
      case "application":
        prefix = "application/";
        break;
      case "audio":
        prefix = "audio/";
        break;
      case "image":
        prefix = "image/";
        break;
      case "linkeddata":
        prefix = "application/ld";
        break;
      case "text":
        prefix = "text/";
        break;
      case "video":
        prefix = "video/";
        break;
      default:
        LOGGER.warn("Unsupported mimeType for type='{}'", type);
        prefix = null;
    }
    if (prefix != null) {
      Filtering mimeFiltering =
          Filtering.builder()
              .add(FilterCriterion.builder().withExpression("mimeType").startsWith(prefix).build())
              .build();
      pageRequest.add(mimeFiltering);
    }
    return service.find(pageRequest);
  }

  @Operation(
      summary = "Get a fileresource by namespace and id",
      description =
          "Separate namespace and id with a colon, e.g. foo:bar. It is also possible, to add a .json suffix, which will be ignored then")
  @GetMapping(
      value = {
        "/v6/fileresources/identifier/**",
        "/v5/fileresources/identifier/**",
        "/v2/fileresources/identifier/**",
        "/latest/fileresources/identifier/**"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Override
  public ResponseEntity<FileResource> getByIdentifier(HttpServletRequest request)
      throws ServiceException, ValidationException {
    return super.getByIdentifier(request);
  }

  @Operation(summary = "Get a fileresource by uuid")
  @GetMapping(
      value = {
        "/v6/fileresources/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/fileresources/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v2/fileresources/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/latest/fileresources/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<FileResource> getByUuid(
      @Parameter(
              example = "",
              description =
                  "UUID of the fileresource, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @Parameter(
              name = "pLocale",
              description =
                  "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false)
          Locale pLocale)
      throws ServiceException {
    if (pLocale == null) {
      return super.getByUuid(uuid);
    } else {
      return super.getByUuidAndLocale(uuid, pLocale);
    }
  }

  @Override
  @Operation(summary = "Get languages of all websites")
  @GetMapping(
      value = {
        "/v6/fileresources/languages",
        "/v5/fileresources/languages",
        "/v2/fileresources/languages",
        "/latest/fileresources/languages"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Locale> getLanguages() throws ServiceException {
    return super.getLanguages();
  }

  @Override
  protected IdentifiableService<FileResource> getService() {
    return service;
  }

  @Override
  @Operation(summary = "Save a newly created fileresource")
  @PostMapping(
      value = {
        "/v6/fileresources",
        "/v5/fileresources",
        "/v2/fileresources",
        "/latest/fileresources"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public FileResource save(@RequestBody FileResource fileResource, BindingResult bindingResult)
      throws ServiceException, ValidationException {
    return super.save(fileResource, bindingResult);
  }

  @Override
  @Operation(summary = "Update a fileresource")
  @PutMapping(
      value = {
        "/v6/fileresources/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v5/fileresources/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/v2/fileresources/{uuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/latest/fileresources/{uuid:" + ParameterHelper.UUID_PATTERN + "}"
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public FileResource update(
      @PathVariable UUID uuid, @RequestBody FileResource fileResource, BindingResult errors)
      throws ServiceException, ValidationException {
    return super.update(uuid, fileResource, errors);
  }
}
