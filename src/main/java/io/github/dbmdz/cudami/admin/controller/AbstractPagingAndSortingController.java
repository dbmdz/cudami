package io.github.dbmdz.cudami.admin.controller;

import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import de.digitalcollections.model.text.LocalizedText;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.dbmdz.cudami.admin.business.i18n.LanguageService;
import io.github.dbmdz.cudami.admin.model.bootstraptable.BTRequest;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import org.apache.commons.lang3.reflect.FieldUtils;

@SuppressFBWarnings
public abstract class AbstractPagingAndSortingController extends AbstractController {

  protected final LanguageService languageService;

  public AbstractPagingAndSortingController(LanguageService languageService) {
    this.languageService = languageService;
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
    // FIXME: add filtering by description additionally to label
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
        // FIXME: Does `dataLanguage` contain the script, e.g. "de-Latn"? What about the DB?
        dataLanguage = getDataLanguage(dataLanguage, languageService);
        // convention: add datalanguage as "sub"-expression to expression - to be handled later on
        // serverside
        expression += "." + dataLanguage;
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
    if (sortProperty == null) {
      return null;
    }

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

  protected String getDataLanguage(String targetDataLanguage, LanguageService languageService)
      throws TechnicalException {
    return getDataLanguage(targetDataLanguage, null, languageService);
  }

  protected String getDataLanguage(
      String targetDataLanguage, List<Locale> existingLanguages, LanguageService languageService)
      throws TechnicalException {
    String dataLanguage = targetDataLanguage;
    if (dataLanguage == null && languageService != null) {
      dataLanguage = languageService.getDefaultLanguage().getLanguage();
    }
    if (existingLanguages != null
        && !existingLanguages.isEmpty()
        && !existingLanguages.contains(Locale.forLanguageTag(dataLanguage))) {
      dataLanguage = existingLanguages.get(0).toLanguageTag();
    }
    return dataLanguage;
  }

  private boolean isMultiLanguageField(Class clz, String fieldName) throws TechnicalException {
    Field field;
    try {
      Class fieldType = getFieldType(clz, fieldName);
      if (LocalizedText.class == fieldType || LocalizedStructuredContent.class == fieldType) {
        return true;
      }
      return false;
    } catch (NoSuchFieldException | SecurityException e) {
      throw new TechnicalException(
          "Field " + fieldName + " not found in class " + clz.getSimpleName(), e);
    }
  }

  /**
   * Get Class of a field of a given class.
   *
   * @param clz class to search in
   * @param fieldName name of field
   * @return Class/Type of field (if found)
   * @throws NoSuchFieldException thrown if not found
   */
  public static Class getFieldType(Class clz, String fieldName) throws NoSuchFieldException {
    Field field = FieldUtils.getField(clz, fieldName, true);
    if (field == null) {
      throw new NoSuchFieldException();
    }
    return field.getType();
  }
}
