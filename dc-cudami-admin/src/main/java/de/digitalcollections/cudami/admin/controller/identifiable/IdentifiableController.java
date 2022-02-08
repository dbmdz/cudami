package de.digitalcollections.cudami.admin.controller.identifiable;

import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.CudamiIdentifiablesClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.Identifiable;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class IdentifiableController extends AbstractController {

  CudamiIdentifiablesClient service;

  public IdentifiableController(CudamiClient client) {
    this.service = client.forIdentifiables();
  }

  @GetMapping(value = "/identifiables")
  @ResponseBody
  public List<Identifiable> find(@RequestParam(name = "term") String searchTerm)
      throws TechnicalException {
    return service.find(searchTerm, 25);
  }
}
