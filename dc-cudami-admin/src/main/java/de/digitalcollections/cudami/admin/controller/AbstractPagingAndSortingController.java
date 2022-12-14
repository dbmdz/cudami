package de.digitalcollections.cudami.admin.controller;

import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.util.LanguageSortingHelper;
import de.digitalcollections.cudami.client.CudamiLocalesClient;
import de.digitalcollections.cudami.client.CudamiRestClient;
import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.text.LocalizedText;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.CollectionUtils;

public abstract class AbstractPagingAndSortingController<T extends UniqueObject>
    extends AbstractController {

  @SuppressFBWarnings
  protected PageRequest createPageRequest(
      String sort,
      String order,
      String dataLanguage,
      CudamiLocalesClient localeService,
      int offset,
      int limit,
      String searchTerm)
      throws TechnicalException {
    Sorting sorting = null;
    if (sort != null && order != null) {
      Order sortingOrder;
      if ("label".equals(sort)) {
        String language = getDataLanguage(dataLanguage, localeService);
        sortingOrder =
            Order.builder()
                .property("label")
                .subProperty(language)
                .direction(Direction.fromString(order))
                .build();
      } else {
        sortingOrder =
            Order.builder().property(sort).direction(Direction.fromString(order)).build();
      }
      sorting = Sorting.builder().order(sortingOrder).build();
    }
    PageRequest pageRequest =
        PageRequest.builder()
            .pageNumber((int) Math.ceil(offset / limit))
            .pageSize(limit)
            .searchTerm(searchTerm)
            .sorting(sorting)
            .build();
    return pageRequest;
  }

  public PageResponse<T> find(
      CudamiLocalesClient localeService,
      CudamiRestClient<T> service,
      int offset,
      int limit,
      String searchTerm,
      String sort,
      String order,
      String dataLanguage)
      throws TechnicalException {

    PageRequest pageRequest =
        createPageRequest(sort, order, dataLanguage, localeService, offset, limit, searchTerm);
    PageResponse<T> pageResponse = service.find(pageRequest);
    return pageResponse;
  }

  protected String getDataLanguage(String targetDataLanguage, CudamiLocalesClient localeService)
      throws TechnicalException {
    String dataLanguage = targetDataLanguage;
    if (dataLanguage == null && localeService != null) {
      dataLanguage = localeService.getDefaultLanguage().getLanguage();
    }
    return dataLanguage;
  }

  protected List<Locale> getExistingLanguages(
      LocalizedText localizedText, LanguageSortingHelper languageSortingHelper) {
    List<Locale> existingLanguages = Collections.emptyList();
    if (!CollectionUtils.isEmpty(localizedText)) {
      existingLanguages =
          languageSortingHelper.sortLanguages(
              LocaleContextHolder.getLocale(), localizedText.getLocales());
    }
    return existingLanguages;
  }

  protected List<Locale> getExistingLanguages(
      List<Locale> locales, LanguageSortingHelper languageSortingHelper) {
    List<Locale> existingLanguages = Collections.emptyList();
    if (!CollectionUtils.isEmpty(locales)) {
      existingLanguages =
          languageSortingHelper.sortLanguages(LocaleContextHolder.getLocale(), locales);
    }
    return existingLanguages;
  }
}
