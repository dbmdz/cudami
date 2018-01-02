package de.digitalcollections.cudami.client.controller.identifiable;

import de.digitalcollections.commons.springmvc.controller.AbstractController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for books management pages.
 */
@Controller
@SessionAttributes(value = {"website"})
public class WebsiteNodesController extends AbstractController implements MessageSourceAware {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebsiteNodesController.class);

  private MessageSource messageSource;

  @Override
  public void setMessageSource(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @ModelAttribute("menu")
  protected String module() {
    return "websites";
  }

  @RequestMapping(value = "/website/{id}/edit", method = RequestMethod.GET)
  public String edit(@PathVariable long id, Model model, RedirectAttributes redirectAttributes) {
    return "website/edit";
  }

  @RequestMapping(value = "/website", method = RequestMethod.GET)
  public String list(Model model) {
    return "website/list";
  }

  @RequestMapping(value = "/website/{id}", method = RequestMethod.GET)
  public String view(@PathVariable long id, Model model) {
    return "website/view";
  }
}
