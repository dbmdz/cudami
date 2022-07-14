package de.digitalcollections.cudami.admin.controller.identifiable.entity.work;

import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.entity.work.CudamiItemsClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.work.Item;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/** Controller for item management pages. */
@Controller
public class ItemsController {
  private final LanguageSortingHelper languageSortingHelper;
  private final CudamiItemsClient service;

  public ItemsController(LanguageSortingHelper languageSortingHelper, CudamiClient client) {
    this.languageSortingHelper = languageSortingHelper;
    this.service = client.forItems();
  }

  @GetMapping("/api/items")
  @ResponseBody
  public PageResponse<Item> find(
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "searchTerm", required = false) String searchTerm,
      @RequestParam(name = "sortBy", required = false) List<Order> sortBy)
      throws TechnicalException {
    PageRequest pageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    return service.find(pageRequest);
  }

  @GetMapping("/api/items/{uuid}/digitalobjects")
  @ResponseBody
  public PageResponse<DigitalObject> getDigitalObjects(
      @PathVariable UUID uuid,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(name = "pageSize", required = false, defaultValue = "25") int pageSize,
      @RequestParam(name = "searchTerm", required = false) String searchTerm)
      throws TechnicalException {
    PageRequest pageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    return service.findDigitalObjects(uuid, pageRequest);
  }

  @GetMapping("/items")
  public String list(Model model) throws TechnicalException {
    final Locale displayLocale = LocaleContextHolder.getLocale();
    model.addAttribute(
        "existingLanguages",
        languageSortingHelper.sortLanguages(displayLocale, service.getLanguages()));
    return "items/list";
  }

  @ModelAttribute("menu")
  protected String module() {
    return "items";
  }

  @GetMapping("/items/{uuid}")
  public String view(@PathVariable UUID uuid, Model model)
      throws TechnicalException, ResourceNotFoundException {
    Item item = service.getByUuid(uuid);
    if (item == null) {
      throw new ResourceNotFoundException();
    }

    Locale displayLocale = LocaleContextHolder.getLocale();
    List<Locale> existingDigitalObjectLanguages =
        languageSortingHelper.sortLanguages(
            displayLocale, service.getLanguagesOfDigitalObjects(uuid));
    model
        .addAttribute("item", item)
        .addAttribute("existingDigitalObjectLanguages", existingDigitalObjectLanguages);
    return "items/view";
  }
}
