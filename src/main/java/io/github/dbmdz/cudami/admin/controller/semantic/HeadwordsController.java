package io.github.dbmdz.cudami.admin.controller.semantic;

import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.semantic.Headword;
import io.github.dbmdz.cudami.admin.business.i18n.LanguageService;
import io.github.dbmdz.cudami.admin.controller.AbstractUniqueObjectController;
import io.github.dbmdz.cudami.admin.controller.ParameterHelper;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/** Controller for headwords management pages. */
@Controller
public class HeadwordsController extends AbstractUniqueObjectController<Headword> {

  private static final Logger LOGGER = LoggerFactory.getLogger(HeadwordsController.class);

  public HeadwordsController(CudamiClient client, LanguageService languageService) {
    super(client.forHeadwords(), languageService);
  }

  @GetMapping("/headwords/new")
  public String create(Model model) throws TechnicalException {
    model.addAttribute("activeLanguage", languageService.getDefaultLanguage());
    return "headwords/create";
  }

  @GetMapping("/headwords/{uuid:" + ParameterHelper.UUID_PATTERN + "}/edit")
  public String edit(
      @PathVariable UUID uuid,
      @RequestParam(name = "activeLanguage", required = false) Locale activeLanguage,
      Model model)
      throws TechnicalException, ResourceNotFoundException {
    Headword headword = service.getByUuid(uuid);
    if (headword == null) {
      throw new ResourceNotFoundException();
    }
    model.addAttribute("headword", headword);
    return "headwords/edit";
  }

  @GetMapping("/headwords")
  public String list(Model model) throws TechnicalException {
    return "headwords/list";
  }

  @ModelAttribute("menu")
  protected String module() {
    return "headwords";
  }

  @GetMapping("/headwords/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  public String view(@PathVariable UUID uuid, Model model)
      throws TechnicalException, ResourceNotFoundException {
    Headword headword = service.getByUuid(uuid);
    if (headword == null) {
      throw new ResourceNotFoundException();
    }
    model.addAttribute("headword", headword);
    return "headwords/view";
  }
}
