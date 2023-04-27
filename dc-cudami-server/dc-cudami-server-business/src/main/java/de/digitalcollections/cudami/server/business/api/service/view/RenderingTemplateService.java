package de.digitalcollections.cudami.server.business.api.service.view;

import de.digitalcollections.cudami.server.business.api.service.UniqueObjectService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.model.view.RenderingTemplate;
import java.util.List;
import java.util.Locale;

public interface RenderingTemplateService extends UniqueObjectService<RenderingTemplate> {

  List<Locale> getLanguages() throws ServiceException;
}
