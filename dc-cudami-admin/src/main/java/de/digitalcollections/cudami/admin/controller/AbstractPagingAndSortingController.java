package de.digitalcollections.cudami.admin.controller;

import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.cudami.admin.business.i18n.LanguageService;
import de.digitalcollections.cudami.admin.model.bootstraptable.BTRequest;
import de.digitalcollections.cudami.admin.model.bootstraptable.BTResponse;
import de.digitalcollections.cudami.client.CudamiRestClient;
import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import de.digitalcollections.model.text.LocalizedText;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.reflect.Field;
import java.util.List;

@SuppressFBWarnings
public abstract class AbstractPagingAndSortingController<T extends UniqueObject>
    extends AbstractController {

  protected final LanguageService languageService;
  protected final CudamiRestClient<T> service;

  public AbstractPagingAndSortingController(
      CudamiRestClient<T> service, LanguageService languageService) {
    this.languageService = languageService;
    this.service = service;
  }

  protected BTRequest createBTRequest(
      Class targetClass,
      int offset,
      int limit,
      String sortProperty,
      String sortOrder,
      String searchProperty,
      String searchTerm,
      String dataLanguage)
      throws TechnicalException {
    BTRequest btRequest;
    if (isMultiLanguageField(targetClass, sortProperty)) {
      if (dataLanguage == null && languageService != null) {
        dataLanguage = languageService.getDefaultLanguage().getLanguage();
      }
      btRequest = new BTRequest(offset, limit, sortProperty, sortOrder, dataLanguage);
    } else {
      btRequest = new BTRequest(offset, limit, sortProperty, sortOrder);
    }

    if (searchTerm != null) {
      Filtering filtering;
      if (isMultiLanguageField(targetClass, searchProperty)) {
        // TODO replace searchTerm param with filtering and remove special searchterm
        // handling in pagerequest on server side
        btRequest.setSearchTerm(searchTerm);
      } else {
        filtering =
            Filtering.builder()
                .add(
                    FilterCriterion.builder()
                        .withExpression(searchProperty)
                        .contains(searchTerm)
                        .build())
                .build();
        btRequest.setFiltering(filtering);
      }
    }
    return btRequest;
  }

  @SuppressFBWarnings
  protected PageRequest createPageRequest(int offset, int limit, String sort, String order) {
    Sorting sorting = null;
    if (sort != null && order != null) {
      Order sortingOrder =
          Order.builder().property(sort).direction(Direction.fromString(order)).build();
      sorting = Sorting.builder().order(sortingOrder).build();
    }
    PageRequest pageRequest =
        PageRequest.builder()
            .pageNumber((int) Math.ceil(offset / limit))
            .pageSize(limit)
            .sorting(sorting)
            .build();
    return pageRequest;
  }

  @SuppressFBWarnings
  protected PageRequest createPageRequest(
      int pageNumber, int pageSize, String searchField, String searchTerm, List<Order> sortBy) {
    PageRequest pageRequest;
    if (searchField == null) {
      pageRequest = new PageRequest(searchTerm, pageNumber, pageSize);
    } else {
      pageRequest = new PageRequest(pageNumber, pageSize);
    }

    if (sortBy != null) {
      Sorting sorting = new Sorting(sortBy);
      pageRequest.setSorting(sorting);
    }
    return pageRequest;
  }

  @SuppressFBWarnings
  protected PageRequest createPageRequest(
      String sort,
      String order,
      String dataLanguage,
      LanguageService languageService,
      int offset,
      int limit,
      String searchTerm)
      throws TechnicalException {
    Sorting sorting = null;
    if (sort != null && order != null) {
      Order sortingOrder;
      if ("label".equals(sort) && dataLanguage != null) {
        String language = getDataLanguage(dataLanguage, languageService);
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

  protected BTResponse<T> find(
      Class targetClass,
      int offset,
      int limit,
      String sortProperty,
      String sortOrder,
      String searchProperty,
      String searchTerm,
      String dataLanguage)
      throws TechnicalException {
    BTRequest btRequest =
        createBTRequest(
            targetClass,
            offset,
            limit,
            sortProperty,
            sortOrder,
            searchProperty,
            searchTerm,
            dataLanguage);
    PageResponse<T> pageResponse = service.find(btRequest);
    return new BTResponse<>(pageResponse);
  }

  public PageResponse<T> find(
      LanguageService languageService,
      CudamiRestClient<T> service,
      int offset,
      int limit,
      String searchTerm,
      String sort,
      String order,
      String dataLanguage)
      throws TechnicalException {

    PageRequest pageRequest =
        createPageRequest(sort, order, dataLanguage, languageService, offset, limit, searchTerm);
    PageResponse<T> pageResponse = service.find(pageRequest);
    return pageResponse;
  }

  protected String getDataLanguage(String targetDataLanguage, LanguageService languageService)
      throws TechnicalException {
    String dataLanguage = targetDataLanguage;
    if (dataLanguage == null && languageService != null) {
      dataLanguage = languageService.getDefaultLanguage().getLanguage();
    }
    return dataLanguage;
  }

  private boolean isMultiLanguageField(Class clz, String fieldName) throws TechnicalException {
    Field field;
    try {
      field = clz.getDeclaredField(fieldName);
      Class fieldTypeClass = field.getType();
      if (LocalizedText.class == fieldTypeClass
          || LocalizedStructuredContent.class == fieldTypeClass) {
        return true;
      }
      return false;
    } catch (NoSuchFieldException | SecurityException e) {
      // for now it is save to return false, as multilingual fields should be found...
      // FIXME: Problem was: "created" was not found...
      return false;
      //      throw new TechnicalException("Field " + fieldName + " in class " +
      // clz.getSimpleName(), e);
    }
  }
}
