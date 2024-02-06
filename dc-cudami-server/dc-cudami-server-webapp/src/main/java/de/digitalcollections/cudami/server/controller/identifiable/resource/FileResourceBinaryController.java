package de.digitalcollections.cudami.server.controller.identifiable.resource;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.FileResourceBinaryService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.FileResourceMetadataService;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.validation.ValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import javax.servlet.http.HttpServletRequest;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "File upload controller")
public class FileResourceBinaryController {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileResourceBinaryController.class);

  private final FileResourceBinaryService binaryService;
  private final FileResourceMetadataService<FileResource> metadataService;

  public FileResourceBinaryController(
      FileResourceBinaryService binaryService,
      @Qualifier("fileResourceMetadataService")
          FileResourceMetadataService<FileResource> metadataService) {
    this.binaryService = binaryService;
    this.metadataService = metadataService;
  }

  @Operation(summary = "Save a file to the disk")
  @PostMapping(
      value = {"/v6/files", "/v5/files", "/v2/files", "/latest/files"},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public FileResource upload(HttpServletRequest request) throws IOException, ValidationException {
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

          fileResource = metadataService.createByMimeType(MimeType.fromTypename(contentType));
          fileResource.setFilename(originalFilename);
          LOGGER.info("filename = " + fileResource.getFilename());

          binaryService.save(fileResource, stream);
          LOGGER.info(
              "saved file '"
                  + fileResource.getUri().toString()
                  + "' ("
                  + fileResource.getSizeInBytes()
                  + " bytes)");

          stream.close();
        }
      }
    } catch (IOException | ServiceException ex) {
      LOGGER.error("Error getting binary data from uploaded file", ex);
    } finally {
      if (stream != null) {
        stream.close();
      }
    }

    return fileResource;
  }
}
