package io.github.dbmdz.cudami.controller.identifiable.entity;

import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiEntitiesClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.Entity;
import io.github.dbmdz.cudami.business.i18n.LanguageService;
import io.github.dbmdz.cudami.controller.ParameterHelper;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class EntitiesController
    extends AbstractEntitiesController<Entity, CudamiEntitiesClient<Entity>> {

  public EntitiesController(CudamiClient cudamiClient, LanguageService languageService) {
    super(cudamiClient.forEntities(), cudamiClient, languageService);
  }

  @GetMapping("/entities/{uuid:" + ParameterHelper.UUID_PATTERN + "}")
  public String view(@PathVariable UUID uuid, Model model)
      throws TechnicalException, ResourceNotFoundException {
    Entity entity = service.getByUuid(uuid);
    if (entity == null) {
      throw new ResourceNotFoundException();
    }
    return doForward(entity, model);
  }

  @GetMapping("/entities/{uuid:" + ParameterHelper.UUID_PATTERN + "}/edit")
  public String edit(@PathVariable UUID uuid, Model model)
      throws TechnicalException, ResourceNotFoundException {
    Entity entity = service.getByUuid(uuid);
    if (entity == null) {
      throw new ResourceNotFoundException();
    }
    return doForward(entity, model) + "/edit";
  }
}
