package de.digitalcollections.cudami.template.website.sidebar.controller;

import de.digitalcollections.cudami.template.website.sidebar.service.ContentService;
import de.digitalcollections.model.identifiable.web.Webpage;
import java.lang.invoke.MethodHandles;
import java.util.Locale;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
/** see GlobalControllerAdvice for global model objects */
public class MainController {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final ContentService contentService;

  public MainController(ContentService contentService) {
    this.contentService = contentService;
  }

  @GetMapping(value = {"", "/"})
  public String home(Model model) {
    log.info("Homepage requested");
    return "index";
  }

  /**
   * Generic webpage page
   *
   * @param uuid uuid of webpage to show
   * @param model the model
   * @return view
   */
  @GetMapping(value = {"/p/{uuid}"})
  public String viewWebpage(@PathVariable UUID uuid, Model model, HttpServletResponse resp) {
    Pair<Webpage, Locale> tuple = contentService.getWebpage(uuid);
    Webpage webpage = tuple.getLeft();
    if (webpage == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    final Locale locale = tuple.getRight();
    model.addAttribute("locale", locale);
    model.addAttribute("webpage", webpage);
    return "webpage";
  }
}
