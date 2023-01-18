package de.digitalcollections.cudami.admin.controller.identifiable;

import de.digitalcollections.cudami.admin.controller.AbstractPagingAndSortingController;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiLocalesClient;
import de.digitalcollections.cudami.client.identifiable.CudamiIdentifiablesClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.Identifiable;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.CollectionUtils;

public class AbstractIdentifiablesController<
        I extends Identifiable, C extends CudamiIdentifiablesClient<I>>
    extends AbstractPagingAndSortingController<I> {

  protected final C service;
  protected final CudamiLocalesClient localeService;

  protected AbstractIdentifiablesController(
      C service, LanguageSortingHelper languageSortingHelper, CudamiLocalesClient localeService) {
    super(languageSortingHelper);
    this.service = service;
    this.localeService = localeService;
  }

  protected List<Locale> getExistingLanguagesFromService() throws TechnicalException {
    List<Locale> serviceLocales = service.getLanguages();
    List<Locale> existingLanguages = Collections.emptyList();
    if (!CollectionUtils.isEmpty(serviceLocales)) {
      existingLanguages =
          languageSortingHelper.sortLanguages(LocaleContextHolder.getLocale(), serviceLocales);
    }
    return existingLanguages;
  }

  protected List<Locale> getExistingLanguagesFromIdentifiables(
      List<? extends Identifiable> identifiables) {
    List<Locale> existingLanguages = Collections.emptyList();
    if (!CollectionUtils.isEmpty(identifiables)) {
      existingLanguages =
          identifiables.stream()
              .flatMap(
                  child ->
                      Stream.concat(
                          child.getLabel().getLocales().stream(),
                          child.getDescription() != null
                              ? child.getDescription().keySet().stream()
                              : Stream.of()))
              .collect(Collectors.toList());
      existingLanguages =
          languageSortingHelper.sortLanguages(LocaleContextHolder.getLocale(), existingLanguages);
    }
    return existingLanguages;
  }

  protected List<Locale> getExistingLanguagesFromIdentifiable(Identifiable identifiable) {
    return getExistingLanguagesFromIdentifiables(List.of(identifiable));
  }
}
