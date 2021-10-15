package de.digitalcollections.cudami.admin.controller.identifiable.alias;

import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.cudami.client.identifiable.alias.CudamiUrlAliasClient;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

/** Controller for urlaliases. */
@Controller
public class UrlAliasesController extends AbstractController {

  private final CudamiUrlAliasClient service;

  public UrlAliasesController(CudamiClient client) {
    this.service = client.forUrlAliases();
  }

  @GetMapping(
      produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8",
      value = {
        "/api/urlaliases/slug/{pLocale}/{label}/{websiteUuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}",
        "/api/urlaliases/slug/{pLocale}/{label}"
      })
  @ResponseBody
  public String generateSlug(
      @PathVariable Locale pLocale,
      @PathVariable String label,
      @PathVariable(required = false) UUID websiteUuid)
      throws HttpException {
    return service.generateSlug(pLocale, label, websiteUuid);
  }
}
