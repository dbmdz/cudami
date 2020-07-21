package de.digitalcollections.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.commons.springdata.domain.PageConverter;
import de.digitalcollections.commons.springdata.domain.PageWrapper;
import de.digitalcollections.commons.springdata.domain.PageableConverter;
import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiDigitalObjectsClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.SessionAttributes;

/** Controller for digital objects management pages. */
@Controller
@SessionAttributes(value = {"digitalobject"})
public class DigitalObjectsController extends AbstractController {

  private static final Logger LOGGER = LoggerFactory.getLogger(DigitalObjectsController.class);

  private final CudamiDigitalObjectsClient service;

  @Autowired
  public DigitalObjectsController(CudamiClient client) {
    this.service = client.forDigitalObjects();
  }

  @ModelAttribute("menu")
  protected String module() {
    return "digitalobjects";
  }

  @GetMapping("/digitalobjects")
  public String list(
      Model model,
      @PageableDefault(
              sort = {"lastModified"},
              size = 25)
          Pageable pageable)
      throws HttpException {
    final PageRequest pageRequest = PageableConverter.convert(pageable);
    final PageResponse pageResponse = service.find(pageRequest);
    Page page = PageConverter.convert(pageResponse, pageRequest);
    model.addAttribute("page", new PageWrapper(page, "/digitalobjects"));
    return "digitalobjects/list";
  }

  @GetMapping("/digitalobjects/{uuid}")
  public String view(@PathVariable UUID uuid, Model model) throws HttpException {
    DigitalObject digitalObject = (DigitalObject) service.findOne(uuid);
    model.addAttribute("availableLocales", digitalObject.getLabel().getLocales());
    model.addAttribute("digitalObject", digitalObject);
    return "digitalobjects/view";
  }
}
