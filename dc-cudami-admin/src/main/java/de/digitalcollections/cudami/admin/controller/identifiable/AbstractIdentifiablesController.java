package de.digitalcollections.cudami.admin.controller.identifiable;

import de.digitalcollections.cudami.admin.business.i18n.LanguageService;
import de.digitalcollections.cudami.admin.controller.AbstractPagingAndSortingController;
import de.digitalcollections.cudami.admin.controller.ParameterHelper;
import de.digitalcollections.cudami.client.identifiable.CudamiIdentifiablesClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.CollectionUtils;

public class AbstractIdentifiablesController<
        I extends Identifiable, C extends CudamiIdentifiablesClient<I>>
    extends AbstractPagingAndSortingController<I> {

  protected AbstractIdentifiablesController(C service, LanguageService languageService) {
    super(service, languageService);
  }

  protected List<Locale> getExistingLanguagesFromIdentifiable(Identifiable identifiable) {
    return getExistingLanguagesFromIdentifiables(List.of(identifiable));
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
          languageService.sortLanguages(LocaleContextHolder.getLocale(), existingLanguages);
    }
    return existingLanguages;
  }

  protected List<Locale> getExistingLanguagesFromService() throws TechnicalException {
    List<Locale> serviceLocales = ((CudamiIdentifiablesClient<I>) service).getLanguages();
    List<Locale> existingLanguages = Collections.emptyList();
    if (!CollectionUtils.isEmpty(serviceLocales)) {
      existingLanguages =
          languageService.sortLanguages(LocaleContextHolder.getLocale(), serviceLocales);
    }
    return existingLanguages;
  }

  protected PageResponse<I> search(String searchField, String searchTerm, PageRequest pageRequest)
      throws TechnicalException {
    PageResponse<I> pageResponse;

    if (searchField == null) {
      pageResponse = service.find(pageRequest);
      return pageResponse;
    } else {
      I identifiable;

      switch (searchField) {
        case "label":
          Filtering filtering =
              Filtering.builder()
                  .add(
                      FilterCriterion.builder()
                          .withExpression("label")
                          .isEquals(searchTerm)
                          .build())
                  .build();
          pageRequest.setFiltering(filtering);
          pageResponse = service.find(pageRequest);
          return pageResponse;
        case "uuid":
          identifiable = service.getByUuid(UUID.fromString(searchTerm));
          if (identifiable == null) {
            pageResponse = PageResponse.builder().withContent(new ArrayList<I>()).build();
          } else {
            pageResponse = PageResponse.builder().withContent(identifiable).build();
          }
          pageResponse.setRequest(pageRequest);
          return pageResponse;
        case "identifier":
          Pair<String, String> namespaceAndId = ParameterHelper.extractPairOfStrings(searchTerm);
          identifiable =
              ((CudamiIdentifiablesClient<I>) service).getByIdentifier(namespaceAndId.getLeft(), namespaceAndId.getRight());
          if (identifiable == null) {
            pageResponse = PageResponse.builder().withContent(new ArrayList<I>()).build();
          } else {
            pageResponse = PageResponse.builder().withContent(identifiable).build();
          }
          pageResponse.setRequest(pageRequest);
          return pageResponse;
        default:
          return null;
      }
    }
  }
}
