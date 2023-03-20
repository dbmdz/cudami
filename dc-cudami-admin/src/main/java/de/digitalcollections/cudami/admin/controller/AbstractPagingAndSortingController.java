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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
      throws TechnicalException, IllegalArgumentException {
    // create with paging
    BTRequest btRequest = new BTRequest(offset, limit);

    // add sorting
    Sorting sorting = createSorting(targetClass, sortProperty, sortOrder, dataLanguage);
    btRequest.setSorting(sorting);

    // add filtering
    Filtering filtering = createFiltering(targetClass, searchProperty, searchTerm, dataLanguage);
    btRequest.setFiltering(filtering);
    return btRequest;
  }

  private Filtering createFiltering(
      Class targetClass, String searchProperty, String searchTerm, String dataLanguage)
      throws TechnicalException {
    Filtering filtering = null;
    if (searchProperty != null && searchTerm != null && targetClass != null) {
      String expression = searchProperty;
      if (isMultiLanguageField(targetClass, searchProperty)) {
        dataLanguage = getDataLanguage(dataLanguage, languageService);
        // convention: add datalanguage as "sub"-expression to expression (safe, as
        // properties in Java do not have "_" in name) - to be handled later on
        // serverside
        expression = expression + "_" + dataLanguage;
      }
      // TODO: default operation is "contains" for now, maybe pass other operators
      // (controller) if
      // we want search in non "string" fields....
      filtering =
          Filtering.builder()
              .add(
                  FilterCriterion.builder().withExpression(expression).contains(searchTerm).build())
              .build();
    }
    return filtering;
  }

  @SuppressFBWarnings
  protected PageRequest createPageRequest(
      Class targetClass,
      int pageNumber,
      int pageSize,
      String sortProperty,
      String sortOrder,
      String searchProperty,
      String searchTerm,
      String dataLanguage)
      throws TechnicalException {
    // create with paging
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);

    // add sorting
    Sorting sorting = createSorting(targetClass, sortProperty, sortOrder, dataLanguage);
    pageRequest.setSorting(sorting);

    // add filtering
    Filtering filtering = createFiltering(targetClass, searchProperty, searchTerm, dataLanguage);
    pageRequest.setFiltering(filtering);

    return pageRequest;
  }

  @SuppressFBWarnings
  protected PageRequest createPageRequest(
      Class targetClass,
      int pageNumber,
      int pageSize,
      List<Order> sortBy,
      String searchProperty,
      String searchTerm,
      String dataLanguage)
      throws TechnicalException {
    // create with paging
    PageRequest pageRequest = new PageRequest(pageNumber, pageSize);

    // add sorting
    Sorting sorting = createSorting(targetClass, sortBy, dataLanguage);
    pageRequest.setSorting(sorting);

    // add filtering
    Filtering filtering = createFiltering(targetClass, searchProperty, searchTerm, dataLanguage);
    pageRequest.setFiltering(filtering);

    return pageRequest;
  }

  private Sorting createSorting(
      Class targetClass, String sortProperty, String sortOrder, String dataLanguage)
      throws TechnicalException {
    String sortLanguage = null;
    if (isMultiLanguageField(targetClass, sortProperty)) {
      sortLanguage = getDataLanguage(dataLanguage, languageService);
    }
    List<Order> orders =
        List.of(
            Order.builder()
                .property(sortProperty)
                .subProperty(sortLanguage)
                .direction(Direction.fromString(sortOrder))
                .build());
    Sorting sorting = new Sorting(orders);
    return sorting;
  }

  private Sorting createSorting(Class targetClass, List<Order> sortBy, String dataLanguage)
      throws TechnicalException {
    if (sortBy != null) {
      String sortLanguage = getDataLanguage(dataLanguage, languageService);
      for (Order order : sortBy) {
        String sortProperty = order.getProperty();
        if (isMultiLanguageField(targetClass, sortProperty)) {
          order.setSubProperty(sortLanguage);
        }
      }
      return new Sorting(sortBy);
    }
    return null;
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
      field = getField(clz, fieldName);
      Class fieldTypeClass = field.getType();
      if (LocalizedText.class == fieldTypeClass
          || LocalizedStructuredContent.class == fieldTypeClass) {
        return true;
      }
      return false;
    } catch (NoSuchFieldException | SecurityException e) {
      throw new TechnicalException(
          "Field " + fieldName + " not found in class " + clz.getSimpleName(), e);
    }
  }

  public static Field getField(Class<?> clz, String fieldName) throws NoSuchFieldException {
    List<Field> allFields = getAllFields(clz);
    if (allFields.isEmpty()) {
      throw new NoSuchFieldException();
    }
    Optional<Field> fieldOpt =
        allFields.stream().filter(f -> f.getName().equals(fieldName)).findFirst();
    if (fieldOpt.isPresent()) {
      return fieldOpt.get();
    }
    throw new NoSuchFieldException();
  }

  /**
   * getDeclaredFields only finds fields of current class not the inherited fields. So recursively
   * collect super fields.
   *
   * @param clz class to get all fields for
   * @return list of all fields (recursively)
   */
  public static List<Field> getAllFields(Class<?> clz) {
    List<Field> fields = new ArrayList<Field>();
    for (Class<?> c = clz; c != null; c = c.getSuperclass()) {
      fields.addAll(Arrays.asList(c.getDeclaredFields()));
    }
    return fields;
  }
}
