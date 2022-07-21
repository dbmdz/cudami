package de.digitalcollections.cudami.admin.controller.legal;

import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.legal.CudamiLicensesClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.legal.License;
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

/** Controller for license management pages. */
@Controller
public class LicensesController {

  private final LanguageSortingHelper languageSortingHelper;
  private final CudamiLicensesClient service;

  public LicensesController(LanguageSortingHelper languageSortingHelper, CudamiClient client) {
    this.languageSortingHelper = languageSortingHelper;
    this.service = client.forLicenses();
  }

  @GetMapping("/api/licenses")
  @ResponseBody
  public PageResponse<License> find(
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

  @GetMapping("/licenses")
  public String list() {
    return "licenses/list";
  }

  @ModelAttribute("menu")
  protected String module() {
    return "licenses";
  }

  @GetMapping("/licenses/{uuid}")
  public String view(@PathVariable UUID uuid, Model model)
      throws TechnicalException, ResourceNotFoundException {
    License license = service.getByUuid(uuid);
    if (license == null) {
      throw new ResourceNotFoundException();
    }
    Locale displayLocale = LocaleContextHolder.getLocale();
    List<Locale> existingLanguages =
        languageSortingHelper.sortLanguages(displayLocale, license.getLabel().getLocales());
    model.addAttribute("license", license).addAttribute("existingLanguages", existingLanguages);
    return "licenses/view";
  }
}
