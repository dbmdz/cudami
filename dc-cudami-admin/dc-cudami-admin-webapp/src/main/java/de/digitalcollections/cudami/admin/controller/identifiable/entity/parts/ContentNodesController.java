package de.digitalcollections.cudami.admin.controller.identifiable.entity.parts;

import de.digitalcollections.commons.springdata.domain.PageConverter;
import de.digitalcollections.commons.springdata.domain.PageWrapper;
import de.digitalcollections.commons.springdata.domain.PageableConverter;
import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.backend.impl.repository.LocaleRepository;
import de.digitalcollections.cudami.admin.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.entity.parts.ContentNodeService;
import de.digitalcollections.model.api.identifiable.entity.parts.ContentNode;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller for content node management pages.
 */
@Controller
public class ContentNodesController extends AbstractController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ContentNodesController.class);

  LocaleRepository localeRepository;
  ContentNodeService service;

  @Autowired
  public ContentNodesController(LocaleRepository localeRepository, ContentNodeService service) {
    this.localeRepository = localeRepository;
    this.service = service;
  }

  @ModelAttribute("menu")
  protected String module() {
    return "contentnodes";
  }

  @GetMapping("/contentnodes/new")
  public String create(Model model, @RequestParam("parentType") String parentType, @RequestParam("parentUuid") String parentUuid) {
    model.addAttribute("activeLanguage", localeRepository.getDefaultLanguage());
    model.addAttribute("parentType", parentType);
    model.addAttribute("parentUuid", parentUuid);
    return "contentnodes/create";
  }

  @GetMapping("/api/contentnodes/new")
  @ResponseBody
  public ContentNode create() {
    return (ContentNode) service.create();
  }

  @GetMapping("/contentnodes/{uuid}/edit")
  public String edit(@PathVariable UUID uuid, Model model) {
    ContentNode contentNode = (ContentNode) service.get(uuid);
    model.addAttribute("activeLanguage", localeRepository.getDefaultLanguage());
    model.addAttribute("uuid", contentNode.getUuid());
    return "contentnodes/edit";
  }

  @GetMapping("/api/contentnodes/{uuid}")
  @ResponseBody
  public ContentNode get(@PathVariable UUID uuid) {
    return (ContentNode) service.get(uuid);
  }

  @GetMapping("/contentnodes")
  public String list(Model model, @PageableDefault(sort = {"uuid"}) Pageable pageable) {
    final PageRequest pageRequest = PageableConverter.convert(pageable);
    final PageResponse pageResponse = service.find(pageRequest);
    Page page = PageConverter.convert(pageResponse, pageRequest);
    model.addAttribute("page", new PageWrapper(page, "/contentnodes"));
    return "contentnodes/list";
  }

  @PostMapping("/api/contentnodes/new")
  public ResponseEntity save(
      @RequestBody ContentNode contentNode,
      @RequestParam("parentType") String parentType,
      @RequestParam("parentUuid") UUID parentUuid
  ) throws IdentifiableServiceException {
    ContentNode contentNodeDb = null;
    HttpHeaders headers = new HttpHeaders();
    try {
      if (parentType.equals("contentTree")) {
        contentNodeDb = service.saveWithParentContentTree(contentNode, parentUuid);
        headers.setLocation(URI.create("/contentnodes/" + contentNodeDb.getUuid().toString()));
      } else if (parentType.equals("contentNode")) {
        contentNodeDb = service.saveWithParentContentNode(contentNode, parentUuid);
        headers.setLocation(URI.create("/contentnodes/" + contentNodeDb.getUuid().toString()));
      }
    } catch (Exception e) {
      if (parentType.equals("contentTree")) {
        LOGGER.error("Cannot save top-level content node: ", e);
      } else if (parentType.equals("contentNode")) {
        LOGGER.error("Cannot save content node: ", e);
      }
      headers.setLocation(URI.create("/contentnodes/new"));
    }
    return new ResponseEntity<>(headers, HttpStatus.SEE_OTHER);
  }

  @PutMapping("/api/contentnodes/{uuid}")
  public ResponseEntity update(@PathVariable UUID uuid, @RequestBody ContentNode contentNode) throws IdentifiableServiceException {
    HttpHeaders headers = new HttpHeaders();
    try {
      service.update(contentNode);
      headers.setLocation(URI.create("/contentnodes/" + uuid));
    } catch (Exception e) {
      String message = "Cannot save content node with uuid=" + uuid + ": " + e;
      LOGGER.error(message, e);
      headers.setLocation(URI.create("/contentnodes/" + uuid + "/edit"));
    }
    return new ResponseEntity<>(headers, HttpStatus.SEE_OTHER);
  }

  @GetMapping("/contentnodes/{uuid}")
  public String view(@PathVariable UUID uuid, Model model) {
    ContentNode contentNode = (ContentNode) service.get(uuid);
    model.addAttribute("availableLanguages", contentNode.getLabel().getLocales());
    model.addAttribute("contentNode", contentNode);

    LinkedHashSet<FileResource> relatedFileResources = service.getRelatedFileResources(contentNode);
    model.addAttribute("relatedFileResources", relatedFileResources);

    return "contentnodes/view";
  }
}
