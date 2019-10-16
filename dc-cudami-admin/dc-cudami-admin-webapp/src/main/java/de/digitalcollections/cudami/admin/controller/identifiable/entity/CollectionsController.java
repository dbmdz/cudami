package de.digitalcollections.cudami.admin.controller.identifiable.entity;

import de.digitalcollections.commons.springdata.domain.PageConverter;
import de.digitalcollections.commons.springdata.domain.PageWrapper;
import de.digitalcollections.commons.springdata.domain.PageableConverter;
import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.backend.api.repository.LocaleRepository;
import de.digitalcollections.cudami.admin.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.client.CudamiCollectionsClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.entity.Collection;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.net.URI;
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

/** Controller for collection management pages. */
@Controller
public class CollectionsController extends AbstractController {

  private static final Logger LOGGER = LoggerFactory.getLogger(CollectionsController.class);

  LocaleRepository localeRepository;
  CudamiCollectionsClient cudamiCollectionsClient;

  @Autowired
  public CollectionsController(
      LocaleRepository localeRepository, CudamiCollectionsClient cudamiCollectionsClient) {
    this.localeRepository = localeRepository;
    this.cudamiCollectionsClient = cudamiCollectionsClient;
  }

  @ModelAttribute("menu")
  protected String module() {
    return "collections";
  }

  @GetMapping("/api/collections/new")
  @ResponseBody
  public Collection create() {
    return cudamiCollectionsClient.createCollection();
  }

  @PostMapping("/api/collections/new")
  public ResponseEntity save(
      @RequestBody Collection collection, @RequestParam("parentUuid") UUID parentUuid)
      throws IdentifiableServiceException {
    Collection collectionDb;
    try {
      collectionDb =
          cudamiCollectionsClient.saveCollectionWithParentCollection(collection, parentUuid);
      return ResponseEntity.ok(collectionDb);
    } catch (Exception e) {
      LOGGER.error("Cannot save collection: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @GetMapping("/api/collections/{uuid}")
  @ResponseBody
  public Collection get(@PathVariable UUID uuid) throws HttpException {
    return cudamiCollectionsClient.getCollection(uuid);
  }

  @PutMapping("/api/collections/{uuid}")
  public ResponseEntity update(@PathVariable UUID uuid, @RequestBody Collection collection)
      throws IdentifiableServiceException {
    HttpHeaders headers = new HttpHeaders();
    try {
      cudamiCollectionsClient.updateCollection(collection);
      headers.setLocation(URI.create("/collections/" + uuid));
    } catch (Exception e) {
      String message = "Cannot save collection with uuid=" + uuid + ": " + e;
      LOGGER.error(message, e);
      headers.setLocation(URI.create("/collections/" + uuid + "/edit"));
    }
    return new ResponseEntity<>(headers, HttpStatus.SEE_OTHER);
  }

  @GetMapping("/collections")
  public String list(
      Model model,
      @PageableDefault(
              sort = {"label"},
              size = 25)
          Pageable pageable) {
    final PageRequest pageRequest = PageableConverter.convert(pageable);
    final PageResponse pageResponse = cudamiCollectionsClient.findCollections(pageRequest);
    Page page = PageConverter.convert(pageResponse, pageRequest);
    model.addAttribute("page", new PageWrapper(page, "/collections"));
    return "collections/list";
  }

  @GetMapping("/collections/{uuid}")
  public String view(@PathVariable UUID uuid, Model model) throws HttpException {
    Collection collection = cudamiCollectionsClient.getCollection(uuid);
    model.addAttribute("availableLanguages", collection.getLabel().getLocales());
    model.addAttribute("collection", collection);
    return "collections/view";
  }

  @GetMapping("/collections/new")
  public String create(Model model) {
    model.addAttribute("activeLanguage", localeRepository.getDefaultLanguage());
    // model.addAttribute("parentUuid", parentUuid);
    return "collections/create";
  }

  @GetMapping("/collections/{uuid}/edit")
  public String edit(@PathVariable UUID uuid, Model model) throws HttpException {
    Collection collection = cudamiCollectionsClient.getCollection(uuid);
    model.addAttribute("activeLanguage", localeRepository.getDefaultLanguage());
    model.addAttribute("uuid", collection.getUuid());
    return "collections/edit";
  }
}
