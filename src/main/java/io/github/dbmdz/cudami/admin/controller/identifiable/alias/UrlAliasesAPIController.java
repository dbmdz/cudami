package io.github.dbmdz.cudami.admin.controller.identifiable.alias;

import com.github.openjson.JSONObject;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.alias.CudamiUrlAliasClient;
import de.digitalcollections.model.exception.TechnicalException;
import io.github.dbmdz.cudami.admin.controller.AbstractController;
import io.github.dbmdz.cudami.admin.controller.ParameterHelper;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/** Controller for all public "UrlAliases" endpoints (API). */
@RestController
public class UrlAliasesAPIController extends AbstractController {

  private final CudamiUrlAliasClient service;

  public UrlAliasesAPIController(CudamiClient client) {
    this.service = client.forUrlAliases();
  }

  @GetMapping(
      produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8",
      value = {
        "/api/urlaliases/slug/{pLocale}/{label}/{websiteUuid:" + ParameterHelper.UUID_PATTERN + "}",
        "/api/urlaliases/slug/{pLocale}/{label}"
      })
  @ResponseBody
  public String generateSlug(
      @PathVariable Locale pLocale,
      @PathVariable String label,
      @PathVariable(required = false) UUID websiteUuid)
      throws TechnicalException {
    return JSONObject.quote(service.generateSlug(pLocale, label, websiteUuid));
  }
}
