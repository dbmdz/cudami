package de.digitalcollections.cudami.admin.controller.identifiable.resource;

import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiFileResourcesBinaryClient;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.http.HttpServletRequest;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

  @Autowired
  public FileResourcesBinaryController(CudamiClient cudamiClient) {
    this.service = cudamiClient.forFileResourcesBinary();
  }

  @ModelAttribute("menu")
  protected String module() {
    return "fileresources";
  }

  @PostMapping("/api/files")
  @ResponseBody
  public FileResource upload(HttpServletRequest request, RedirectAttributes redirectAttributes)
      throws InterruptedException, IOException {
    boolean isMultipart = ServletFileUpload.isMultipartContent(request);
    if (!isMultipart) {
      // Inform user about invalid request
      redirectAttributes.addFlashAttribute("message", "Invalid file resource!");
      return null;
    }
    InputStream stream = null;
    try {
      ServletFileUpload upload = new ServletFileUpload();
      FileItemIterator iter = upload.getItemIterator(request);
      while (iter.hasNext()) {
        FileItemStream item = iter.next();
        if (!item.isFormField()) {
          String contentType = item.getContentType();
          String filename = item.getName();
          stream = item.openStream();

          FileResource fileResource = service.upload(stream, filename, contentType);
          return fileResource;
        }
      }
    } catch (IOException | FileUploadException e) {
      LOGGER.error("Error saving uploaded file data", e);
      redirectAttributes.addFlashAttribute("message", "Error saving file resource!");
      return null;
    } finally {
      if (stream != null) {
        stream.close();
      }
    }
    LOGGER.warn("Invalid file resource!");
    return null;
  }
}
