package de.digitalcollections.cudami.server.controller;

import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import java.util.List;
import java.util.Locale;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(description = "The locale/language controller", name = "Locale controller")
public class LocaleController {

  @Autowired
  private LocaleService service;

  @ApiMethod(description = "get all supported locales")
  @RequestMapping(value = {"/latest/locales", "/v2/locales", "/v1/locales"}, method = {RequestMethod.GET})
  @ApiResponseObject
  public List<Locale> getAll() {
    return service.getAll();
  }

  @ApiMethod(description = "get default locale")
  @RequestMapping(value = {"/latest/locales/default", "/v2/locales/default", "/v1/locales/default"}, method = {RequestMethod.GET})
  @ApiResponseObject
  public Locale getDefault() {
    return service.getDefault();
  }
}
