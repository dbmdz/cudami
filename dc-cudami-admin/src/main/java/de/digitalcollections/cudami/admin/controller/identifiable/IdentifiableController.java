package de.digitalcollections.cudami.admin.controller.identifiable;

import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiIdentifiablesClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.impl.identifiable.IdentifiableImpl;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class IdentifiableController extends AbstractController {

  CudamiIdentifiablesClient service;

  @Autowired
  public IdentifiableController(CudamiClient cudamiClient) {
    this.service = cudamiClient.forIdentifiables();
  }

  @GetMapping(value = "/identifiables")
  @ResponseBody
  public List<IdentifiableImpl> find(@RequestParam(name = "term") String searchTerm)
      throws HttpException {
    List<IdentifiableImpl> identifiables = service.find(searchTerm, 25);
    return identifiables;
  }
}
