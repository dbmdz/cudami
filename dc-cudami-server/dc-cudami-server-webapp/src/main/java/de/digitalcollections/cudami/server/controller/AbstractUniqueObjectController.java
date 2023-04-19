package de.digitalcollections.cudami.server.controller;

import de.digitalcollections.cudami.server.business.api.service.UniqueObjectService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
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
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

public abstract class AbstractUniqueObjectController<U extends UniqueObject>
    extends AbstractPagingAndSortingController {

  protected final Logger LOGGER;

  private final Class<U> objectType;

  public AbstractUniqueObjectController() {
    super();
    LOGGER = LoggerFactory.getLogger(getClass());
    objectType =
        (Class<U>)
            ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
  }

  protected long count() throws ServiceException {
    return getService().count();
  }

  protected ResponseEntity delete(UUID uuid) throws ConflictException, ServiceException {
    U example = buildExampleWithUuid(uuid);
    boolean successful;
    try {
      successful = getService().delete(example);
    } catch (ServiceException e) {
      LOGGER.error(
          "Cannot delete "
              + (example != null ? example.getClass().getSimpleName() : "anything")
              + " with uuid="
              + uuid
              + ": "
              + e,
          e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return successful
        ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  protected ResponseEntity<Void> delete(List<UUID> uuids)
      throws ServiceException, ConflictException {
    getService()
        .delete(
            uuids.stream()
                .map(
                    u -> {
                      try {
                        return buildExampleWithUuid(u);
                      } catch (ServiceException e) {
                        throw new RuntimeException(e);
                      }
                    })
                .collect(Collectors.toSet()));
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
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
    U result = getService().getByExample(buildExampleWithUuid(uuid));
    return new ResponseEntity<>(result, result != null ? HttpStatus.OK : HttpStatus.NOT_FOUND);
  }

  protected ResponseEntity<U> getByUuidAndLocale(UUID uuid, Locale locale) throws ServiceException {
    U result = getService().getByExampleAndLocale(buildExampleWithUuid(uuid), locale);
    return new ResponseEntity<>(result, result != null ? HttpStatus.OK : HttpStatus.NOT_FOUND);
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

  protected U buildExampleWithUuid(UUID uuid) throws ServiceException {
    try {
      U example = objectType.getDeclaredConstructor().newInstance();
      example.setUuid(uuid);
      return example;
    } catch (NoSuchMethodException
        | InstantiationException
        | IllegalAccessException
        | InvocationTargetException e) {
      throw new ServiceException(
          "Cannot construct example " + objectType + " for uuid=" + uuid + ": " + e, e);
    }
  }
}
