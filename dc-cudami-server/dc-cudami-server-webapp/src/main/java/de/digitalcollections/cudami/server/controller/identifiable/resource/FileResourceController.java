package de.digitalcollections.cudami.server.controller.identifiable.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.FileResourceService;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.MimeType;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.Sorting;
import de.digitalcollections.model.api.paging.enums.Direction;
import de.digitalcollections.model.api.paging.enums.NullHandling;
import de.digitalcollections.model.impl.identifiable.parts.LocalizedTextImpl;
import de.digitalcollections.model.impl.identifiable.parts.structuredcontent.LocalizedStructuredContentImpl;
import de.digitalcollections.model.impl.paging.OrderImpl;
import de.digitalcollections.model.impl.paging.PageRequestImpl;
import de.digitalcollections.model.impl.paging.SortingImpl;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiPathParam;
import org.jsondoc.core.annotation.ApiQueryParam;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RestController
@Api(description = "The fileresource controller", name = "Fileresource controller")
public class FileResourceController {

  private static final org.slf4j.Logger LOGGER =
      LoggerFactory.getLogger(FileResourceController.class);

  @Autowired FileResourceService fileResourceService;

  @Autowired LocaleService localeService;

  @Autowired private ObjectMapper objectMapper;

  @ApiMethod(description = "Get all fileresources")
  @GetMapping(
      value = {"/latest/fileresources", "/v2/fileresources"},
      produces = "application/json")
  @ApiResponseObject
  public PageResponse<FileResource> findAll(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
      @RequestParam(name = "sortField", required = false, defaultValue = "uuid") String sortField,
      @RequestParam(name = "sortDirection", required = false, defaultValue = "ASC")
          Direction sortDirection,
      @RequestParam(name = "nullHandling", required = false, defaultValue = "NATIVE")
          NullHandling nullHandling) {
    OrderImpl order = new OrderImpl(sortDirection, sortField, nullHandling);
    Sorting sorting = new SortingImpl(order);
    PageRequest pageRequest = new PageRequestImpl(pageNumber, pageSize, sorting);
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

  // FIXME solve conflict of similar post with upload or pure metadata
  @ApiMethod(description = "Save a newly created fileresource")
  @PostMapping(
      value = {"/latest/fileresources2", "/v2/fileresources2"},
      produces = "application/json")
  @ApiResponseObject
  public FileResource save(@RequestBody FileResource fileResource)
      throws IdentifiableServiceException {
    return fileResourceService.save(fileResource);
  }

  @ApiMethod(description = "Save a newly created fileresource")
  @PostMapping(
      value = {"/latest/fileresources", "/v2/fileresources"},
      produces = "application/json")
  @ApiResponseObject
  public ResponseEntity<FileResource> save(
      @RequestParam("fileresource") String resourceJson,
      @RequestPart("binaryData") MultipartFile file,
      RedirectAttributes redirectAttributes,
      HttpServletRequest request) {
    FileResource fileResource;
    try {
      // FIXME: is it really necessary to handle string and convert to object (no direct support of
      // spring boot/mvc?)
      fileResource = objectMapper.readValue(resourceJson, FileResource.class);
      LOGGER.info("resource: " + fileResource.getLabel().getText());
    } catch (IOException ex) {
      LOGGER.error(
          "Error: Cannot convert resource json to resource or read file bytes: '"
              + resourceJson
              + "'",
          ex);
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    InputStream inputStream = null;
    if (!file.isEmpty()) {
      try {
        inputStream = file.getInputStream();
      } catch (IOException ex) {
        LOGGER.error("Error getting binary data from uploaded file", ex);
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
      }
    }

    try {
      fileResourceService.save(fileResource, inputStream);
    } catch (IdentifiableServiceException ex) {
      LOGGER.error("Error saving fileresource and binary data for uploaded file", ex);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return new ResponseEntity<>(fileResource, HttpStatus.OK);
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

  @PostMapping(value = {"/latest/fileresources/new/upload", "/v2/fileresources/new/upload"})
  @ApiResponseObject
  public FileResource apiUpload(HttpServletRequest request) throws IOException {
    FileResource fileResource = null;

    InputStream stream = null;
    try {
      boolean isMultipart = ServletFileUpload.isMultipartContent(request);
      if (!isMultipart) {
        throw new InvalidObjectException("no multipart content");
      }

      ServletFileUpload upload = new ServletFileUpload();

      FileItemIterator iter = upload.getItemIterator(request);
      while (iter.hasNext()) {
        FileItemStream item = iter.next();
        if (!item.isFormField()) {
          stream = item.openStream();
          String originalFilename = item.getName();
          originalFilename = URLDecoder.decode(originalFilename, StandardCharsets.UTF_8.toString());
          String contentType = item.getContentType();

          fileResource = fileResourceService.createByMimeType(MimeType.fromTypename(contentType));
          fileResource.setFilename(originalFilename);
          LOGGER.info("filename = " + fileResource.getFilename());

          // set label to originalfilename for now. can be changed in next step of user input
          fileResource.setDescription(new LocalizedStructuredContentImpl());
          fileResource.setLabel(
              new LocalizedTextImpl(
                  new Locale(localeService.getDefaultLanguage()), originalFilename));

          fileResource = fileResourceService.save(fileResource, stream);
          LOGGER.info(
              "saved file '"
                  + fileResource.getUri().toString()
                  + "' ("
                  + fileResource.getSizeInBytes()
                  + " bytes)");

          stream.close();
        }
      }
    } catch (FileUploadException | IOException | IdentifiableServiceException ex) {
      LOGGER.error("Error getting binary data from uploaded file", ex);
    } finally {
      if (stream != null) {
        stream.close();
      }
    }

    return fileResource;
  }
}
