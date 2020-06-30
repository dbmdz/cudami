package de.digitalcollections.cudami.server.controller.identifiable.entity.parts;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.parts.WebpageService;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import java.util.Locale;
import java.util.UUID;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiPathParam;
import org.jsondoc.core.annotation.ApiQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Api(description = "The webpage HTML controller", name = "Webpage HTML controller")
public class WebpageHtmlController {

  @Autowired private WebpageService webpageService;

  @ApiMethod(description = "Get a webpage as HTML")
  @GetMapping(
      value = {
        "/latest/webpages/{uuid}.html",
        "/v3/webpages/{uuid}.html",
        "/v2/webpages/{uuid}.html",
        "/v1/webpages/{uuid}.html"
      },
      produces = MediaType.TEXT_HTML_VALUE)
  public String getWebpageAsHtml(
      @ApiPathParam(
              description =
                  "UUID of the webpage, e.g. <tt>599a120c-2dd5-11e8-b467-0ed5f89f718b</tt>")
          @PathVariable("uuid")
          UUID uuid,
      @ApiQueryParam(
              name = "pLocale",
              description =
                  "Desired locale, e.g. <tt>de_DE</tt>. If unset, contents in all languages will be returned")
          @RequestParam(name = "pLocale", required = false)
          Locale pLocale,
      @ApiQueryParam(
              name = "renderLabel",
              description =
                  "Switch for (de)activating rendering of webpage label. If 'false', label rendering will be skipped")
          @RequestParam(name = "renderLabel", required = false, defaultValue = "true")
          String renderLabel,
      Model model)
      throws IdentifiableServiceException {
    Webpage webpage;
    if (pLocale == null) {
      webpage = (Webpage) webpageService.get(uuid);
    } else {
      webpage = (Webpage) webpageService.get(uuid, pLocale);
      Locale returnedLocale = getLocale(webpage);
      model.addAttribute("locale", returnedLocale);
    }
    model.addAttribute("renderLabel", Boolean.parseBoolean(renderLabel));
    model.addAttribute("webpage", webpage);
    return "webpage";
  }

  private Locale getLocale(Webpage webpage) {
    if (webpage == null) {
      return null;
    }
    Locale returnedLocale = webpage.getLabel().getLocales().iterator().next();
    return returnedLocale;
  }
}
