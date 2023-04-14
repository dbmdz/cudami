package de.digitalcollections.cudami.server.controller;

import de.digitalcollections.cudami.server.business.api.service.UniqueObjectService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

public abstract class AbstractUniqueObjectController<U extends UniqueObject>
    extends AbstractPagingAndSortingController {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(AbstractUniqueObjectController.class);

  private final Class<U> objectType;

  public AbstractUniqueObjectController() {
    super();
    objectType =
        (Class<U>)
            ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
  }

  protected abstract UniqueObjectService<U> getService();

  protected PageResponse<U> find(
      int pageNumber, int pageSize, List<Order> sortBy, List<FilterCriterion> filterCriteria)
      throws ServiceException {

    PageRequest pageRequest =
        createPageRequest(objectType, pageNumber, pageSize, sortBy, filterCriteria);
    return getService().find(pageRequest);
  }

  protected ResponseEntity<U> getByUuid(UUID uuid) throws ServiceException {
    try {
      U example = objectType.getDeclaredConstructor().newInstance();
      example.setUuid(uuid);
      U result = getService().getByExample(example);
      return new ResponseEntity<>(result, result != null ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    } catch (NoSuchMethodException
        | InstantiationException
        | IllegalAccessException
        | InvocationTargetException e) {
      throw new ServiceException(
          "Cannot construct example " + objectType + " for getByUuid(" + uuid + "): " + e, e);
    }
  }

  protected ResponseEntity<U> getByUuidAndLocale(UUID uuid, Locale locale) throws ServiceException {
    try {
      U example = objectType.getDeclaredConstructor().newInstance();
      example.setUuid(uuid);
      U result = getService().getByExampleAndLocale(example, locale);
      return new ResponseEntity<>(result, result != null ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    } catch (NoSuchMethodException
        | InstantiationException
        | IllegalAccessException
        | InvocationTargetException e) {
      throw new ServiceException(
          "Cannot construct example "
              + objectType
              + " for getByUuidAndLocale("
              + uuid
              + ","
              + locale
              + "): "
              + e,
          e);
    }
  }

  protected U save(U uniqueObject, BindingResult bindingResult)
      throws ValidationException, ServiceException {
    getService().save(uniqueObject);
    return uniqueObject;
  }

  protected U update(UUID uuid, U uniqueObject, BindingResult bindingResult)
      throws ValidationException, ServiceException {
    assert Objects.equals(uuid, uniqueObject.getUuid());
    getService().update(uniqueObject);
    return uniqueObject;
  }
}
