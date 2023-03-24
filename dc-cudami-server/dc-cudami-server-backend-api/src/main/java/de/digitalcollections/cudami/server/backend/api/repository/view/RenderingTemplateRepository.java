package de.digitalcollections.cudami.server.backend.api.repository.view;

import de.digitalcollections.cudami.server.backend.api.repository.UniqueObjectRepository;
import de.digitalcollections.model.view.RenderingTemplate;
import java.util.List;
import java.util.Locale;

/** Repository for rendering templates persistence handling. */
public interface RenderingTemplateRepository extends UniqueObjectRepository<RenderingTemplate> {

  /**
   * Return list of languages of all rendering templates
   *
   * @return list of languages
   */
  List<Locale> getLanguages();
}
