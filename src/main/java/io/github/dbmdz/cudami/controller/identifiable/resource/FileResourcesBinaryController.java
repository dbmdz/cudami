package io.github.dbmdz.cudami.controller.identifiable.resource;

import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.resource.CudamiFileResourcesBinaryClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.resource.FileResource;
import io.github.dbmdz.cudami.controller.AbstractController;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.fileupload2.core.FileItemInput;
import org.apache.commons.fileupload2.core.FileItemInputIterator;
import org.apache.commons.fileupload2.jakarta.JakartaServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Controller for resource management pages. */
@Controller
public class FileResourcesBinaryController extends AbstractController {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileResourcesBinaryController.class);

  CudamiFileResourcesBinaryClient service;

  public FileResourcesBinaryController(CudamiClient client) {
    this.service = client.forFileResourcesBinary();
  }

  @ModelAttribute("menu")
  protected String module() {
    return "fileresources";
  }

  @PostMapping("/api/files")
  @ResponseBody
  public FileResource upload(HttpServletRequest request, RedirectAttributes redirectAttributes)
      throws TechnicalException {
    boolean isMultipart = JakartaServletFileUpload.isMultipartContent(request);
    if (!isMultipart) {
      // Inform user about invalid request
      redirectAttributes.addFlashAttribute("message", "Invalid file resource!");
      return null;
    }
    InputStream stream = null;
    try {
      JakartaServletFileUpload upload = new JakartaServletFileUpload();
      FileItemInputIterator iter = upload.getItemIterator(request);
      while (iter.hasNext()) {
        FileItemInput item = iter.next();
        if (!item.isFormField()) {
          String contentType = item.getContentType();
          String filename = item.getName();
          stream = item.getInputStream();

          FileResource fileResource = service.upload(stream, filename, contentType);
          return fileResource;
        }
      }
    } catch (IOException e) {
      LOGGER.error("Error saving uploaded file data", e);
      redirectAttributes.addFlashAttribute("message", "Error saving file resource!");
      return null;
    } finally {
      if (stream != null) {
        try {
          stream.close();
        } catch (IOException ex) {
          throw new TechnicalException("Error closing stream", ex);
        }
      }
    }
    LOGGER.warn("Invalid file resource!");
    return null;
  }
}
