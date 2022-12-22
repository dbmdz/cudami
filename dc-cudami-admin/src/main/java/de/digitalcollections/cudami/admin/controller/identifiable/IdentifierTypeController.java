package de.digitalcollections.cudami.admin.controller.identifiable;

import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.controller.ParameterHelper;
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.CudamiIdentifierTypesClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.IdentifierType;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

/** Controller for identifier type management pages. */
@Controller
public class IdentifierTypeController extends AbstractController {

  private static final Logger LOGGER = LoggerFactory.getLogger(IdentifierTypeController.class);

  private final CudamiIdentifierTypesClient service;

  public IdentifierTypeController(CudamiClient client) {
    this.service = client.forIdentifierTypes();
  }

  @ModelAttribute("menu")
  protected String module() {
    return "identifiertypes";
  }

  @GetMapping("/identifiertypes/new")
  public String create() {
    return "identifiertypes/create";
  }

  @GetMapping("/identifiertypes/{uuid:" + ParameterHelper.UUID_PATTERN + "}/edit")
  public String edit(@PathVariable UUID uuid, Model model)
      throws TechnicalException, ResourceNotFoundException {
    IdentifierType identifierType = service.getByUuid(uuid);
    if (identifierType == null) {
      throw new ResourceNotFoundException();
    }
    model.addAttribute("label", identifierType.getLabel());
    model.addAttribute("uuid", identifierType.getUuid());
    return "identifiertypes/edit";
  }

  @GetMapping("/identifiertypes")
  public String list() {
    return "identifiertypes/list";
  }

  @GetMapping("/identifiertypes/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  public String view(@PathVariable UUID uuid, Model model)
      throws TechnicalException, ResourceNotFoundException {
    IdentifierType identifierType = service.getByUuid(uuid);
    if (identifierType == null) {
      throw new ResourceNotFoundException();
    }

    model.addAttribute("identifierType", identifierType);
    return "identifiertypes/view";
  }
}
