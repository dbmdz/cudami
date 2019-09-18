package de.digitalcollections.cudami.admin.controller.identifiable;

import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.model.api.identifiable.Identifiable;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class IdentifiableController extends AbstractController {

  IdentifiableService<Identifiable> service;

  @Autowired
  public IdentifiableController(IdentifiableService<Identifiable> service) {
    this.service = service;
  }

  @GetMapping(value = "/identifiables")
  @ResponseBody
  public List<Identifiable> find(@RequestParam(name = "term") String searchTerm) {
    List<Identifiable> identifiables = service.find(searchTerm, 25);
    return identifiables;
  }
}
