package de.digitalcollections.cudami.server.controller.identifiable.resource;

import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.FileResourceMetadataService;
import de.digitalcollections.model.api.filter.Filtering;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.paging.Order;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.SearchPageRequest;
import de.digitalcollections.model.api.paging.SearchPageResponse;
import de.digitalcollections.model.api.paging.Sorting;
import de.digitalcollections.model.impl.paging.PageRequestImpl;
import de.digitalcollections.model.impl.paging.SearchPageRequestImpl;
import de.digitalcollections.model.impl.paging.SortingImpl;
import java.util.List;
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
import org.springframework.beans.factory.annotation.Qualifier;
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
@Api(description = "The fileresource controller", name = "Fileresource controller")
public class FileResourceMetadataController {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(FileResourceMetadataController.class);

  @Autowired
  @Qualifier("fileResourceMetadataServiceImpl")
  private FileResourceMetadataService<FileResource> fileResourceService;

  @Autowired private LocaleService localeService;

  @ApiMethod(description = "Get all fileresources")
  @GetMapping(
      value = {"/latest/fileresources", "/v2/fileresources"},
      produces = "application/json")
  @ApiResponseObject
  public PageResponse<FileResource> findAll(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy) {
    PageRequest pageRequest = new PageRequestImpl(pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new SortingImpl(sortBy);
      pageRequest.setSorting(sorting);
    }
    return fileResourceService.find(pageRequest);
  }

  @ApiMethod(
      description =
          "Find limited amount of fileresources of given type containing searchTerm in label or description")
  @GetMapping(
      value = {"/latest/fileresources/type/{type}", "/v2/fileresources/type/{type}"},
      produces = "application/json")
  @ApiResponseObject
  public SearchPageResponse<FileResource> findFileResourcesByType(
      @ApiPathParam(description = "Type of the fileresource, e.g. <tt>image</tt>")
          @PathVariable("type")
          String type,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy,
      @RequestParam(name = "searchTerm", required = false) String searchTerm) {
    SearchPageRequest pageRequest = new SearchPageRequestImpl(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new SortingImpl(sortBy);
      pageRequest.setSorting(sorting);
    }

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
      Filtering filtering =
          Filtering.defaultBuilder().filter("mimeType").startsWith(prefix).build();
      pageRequest.add(filtering);
    }
    return fileResourceService.find(pageRequest);
  }

  @ApiMethod(
      description =
          "Get a fileresource as JSON or XML, depending on extension or <tt>format</tt> request parameter or accept header")
  @GetMapping(
      value = {"/latest/fileresources/{uuid}", "/v2/fileresources/{uuid}"},
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ApiResponseObject
  public ResponseEntity<FileResource> get(
      @ApiPathParam(
              description =
                  "UUID of the fileresource, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @ApiQueryParam(
              name = "pLocale",
              description =
                  "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false)
          Locale pLocale)
      throws IdentifiableServiceException {
    FileResource fileResource;
    if (pLocale == null) {
      fileResource = fileResourceService.get(uuid);
    } else {
      fileResource = fileResourceService.get(uuid, pLocale);
    }
    return new ResponseEntity<>(fileResource, HttpStatus.OK);
  }

  @ApiMethod(
      description =
          "Get a fileresource as JSON or XML, depending on extension or <tt>format</tt> request parameter or accept header")
  @GetMapping(
      value = {
        "/latest/fileresources/identifier/{namespace}:{id}",
        "/v2/fileresources/identifier/{namespace}:{id}"
      },
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ApiResponseObject
  public ResponseEntity<FileResource> getByIdentifier(
      @PathVariable String namespace, @PathVariable String id) throws IdentifiableServiceException {

    FileResource fileResource = fileResourceService.getByIdentifier(namespace, id);
    return new ResponseEntity<>(fileResource, HttpStatus.OK);
  }

  @ApiMethod(description = "Save a newly created fileresource")
  @PostMapping(
      value = {"/latest/fileresources", "/v2/fileresources"},
      produces = "application/json")
  @ApiResponseObject
  public FileResource save(@RequestBody FileResource fileResource)
      throws IdentifiableServiceException {
    return fileResourceService.save(fileResource);
  }

  @ApiMethod(description = "Update a fileresource")
  @PutMapping(
      value = {"/latest/fileresources/{uuid}", "/v2/fileresources/{uuid}"},
      produces = "application/json")
  @ApiResponseObject
  public FileResource update(
      @PathVariable UUID uuid, @RequestBody FileResource fileResource, BindingResult errors)
      throws IdentifiableServiceException {
    assert Objects.equals(uuid, fileResource.getUuid());
    return fileResourceService.update(fileResource);
  }
}
