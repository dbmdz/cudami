package de.digitalcollections.cudami.server.controller.identifiable.resource;

import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.FileResourceBinaryService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.FileResourceMetadataService;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.MimeType;
import de.digitalcollections.model.impl.identifiable.parts.LocalizedTextImpl;
import de.digitalcollections.model.impl.identifiable.parts.structuredcontent.LocalizedStructuredContentImpl;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(description = "The file upload controller", name = "File upload controller")
public class FileResourceBinaryController {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileResourceBinaryController.class);

  @Autowired FileResourceMetadataService fileResourceService;

  @Autowired FileResourceBinaryService fileUploadService;

  @Autowired LocaleService localeService;

  @PostMapping(value = {"/latest/files", "/v2/files"})
  @ApiResponseObject
  public FileResource upload(HttpServletRequest request) throws IOException {
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

          // set label to original filename for now. can be changed in next step of user input
          fileResource.setLabel(
              new LocalizedTextImpl(
                  new Locale(localeService.getDefaultLanguage()), originalFilename));

          fileResource = fileUploadService.save(fileResource, stream);
          fileResource.setDescription(new LocalizedStructuredContentImpl());
          LOGGER.info(
              "saved file '"
                  + fileResource.getUri().toString()
                  + "' ("
                  + fileResource.getSizeInBytes()
                  + " bytes)");

          stream.close();
        }
      }
    } catch (IOException | IdentifiableServiceException ex) {
      LOGGER.error("Error getting binary data from uploaded file", ex);
    } finally {
      if (stream != null) {
        stream.close();
      }
    }

    return fileResource;
  }
}
