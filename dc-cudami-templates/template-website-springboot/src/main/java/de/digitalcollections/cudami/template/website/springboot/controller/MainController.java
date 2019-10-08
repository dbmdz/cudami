package de.digitalcollections.cudami.template.website.springboot.controller;

import de.digitalcollections.cudami.template.website.springboot.business.LocaleService;
import java.util.Date;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = {"", "/"})
public class MainController {

  private static final Logger LOGGER = LoggerFactory.getLogger(MainController.class);

  @Autowired
  private LocaleService localeService;

  @RequestMapping(method = RequestMethod.GET)
  public String printWelcome(Model model) {
    LOGGER.info("Homepage requested");

    model.addAttribute("time", new Date());
    Locale defaultLanguage = localeService.getDefaultLanguage();
    if (defaultLanguage == null) {
      defaultLanguage = Locale.ENGLISH;
    }
    Locale currentUserLocale = LocaleContextHolder.getLocale();
    model.addAttribute("defaultLanguage", defaultLanguage.getDisplayName(currentUserLocale));

    return "main";
  }
}
