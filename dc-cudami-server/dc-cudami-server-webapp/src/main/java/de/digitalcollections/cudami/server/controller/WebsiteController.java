package de.digitalcollections.cudami.server.controller;

import de.digitalcollections.cudami.server.business.api.service.WebsiteService;
import de.digitalcollections.cudami.model.api.entity.Website;
import java.util.List;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/website")
@Api(description = "The website controller", name = "Website controller")
public class WebsiteController {

  @Autowired
  private WebsiteService websiteService;

  @ApiMethod(description = "get all websites")
  @RequestMapping(value = "/v1/websites", produces = "application/json", method = RequestMethod.GET)
  @ApiResponseObject
  public List<Website> getAll() {
    return websiteService.getAll();
  }

}
