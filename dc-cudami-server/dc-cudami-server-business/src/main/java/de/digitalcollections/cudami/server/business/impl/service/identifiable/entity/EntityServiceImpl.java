package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.EntityService;
import de.digitalcollections.cudami.server.business.impl.service.identifiable.IdentifiableServiceImpl;
import de.digitalcollections.cudami.server.config.HookProperties;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.EntityType;
import de.digitalcollections.model.identifiable.resource.FileResource;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

// @Transactional should not be set in derived class to prevent overriding, check base class instead
@Service("entityService")
public class EntityServiceImpl<E extends Entity> extends IdentifiableServiceImpl<E>
    implements EntityService<E> {

  private static final Logger LOGGER = LoggerFactory.getLogger(EntityServiceImpl.class);

  protected HookProperties hookProperties;

  protected HttpClient httpClient = HttpClient.newHttpClient();

  public EntityServiceImpl(
      @Qualifier("entityRepositoryImpl") EntityRepository<E> repository,
      IdentifierRepository identifierRepository,
      UrlAliasService urlAliasService,
      HookProperties hookProperties,
      LocaleService localeService,
      CudamiConfig cudamiConfig) {
    super(repository, identifierRepository, urlAliasService, localeService, cudamiConfig);
    this.hookProperties = hookProperties;
  }

  @Override
  public void addRelatedFileresource(E entity, FileResource fileResource) {
    ((EntityRepository<E>) repository).addRelatedFileresource(entity, fileResource);
  }

  @Override
  public void addRelatedFileresource(UUID entityUuid, UUID fileResourceUuid) {
    ((EntityRepository<E>) repository).addRelatedFileresource(entityUuid, fileResourceUuid);
  }

  /**
   * Build a notification url by replacing placeholders in the template with the entity's uuid and
   * type
   */
  protected URI buildNotificationUrl(String urlTemplate, UUID entityUuid, EntityType entityType) {
    String url = String.format(urlTemplate, entityUuid, entityType);
    try {
      return new URL(url).toURI();
    } catch (MalformedURLException | URISyntaxException e) {
      LOGGER.warn("Something went wrong when creating the notification url: {}", e.getMessage());
      return null;
    }
  }

  protected Filtering filteringForActive() {
    // business logic that defines, what "active" means
    LocalDate now = LocalDate.now();
    Filtering filtering =
        Filtering.defaultBuilder()
            .filter("publicationStart")
            .lessOrEqualAndSet(now)
            .filter("publicationEnd")
            .greaterOrNotSet(now)
            .build();
    return filtering;
  }

  @Override
  public E getByRefId(long refId) {
    return ((EntityRepository<E>) repository).getByRefId(refId);
  }

  @Override
  public List<E> getRandom(int count) {
    return ((EntityRepository<E>) repository).getRandom(count);
  }

  @Override
  public List<FileResource> getRelatedFileResources(E entity) {
    return ((EntityRepository<E>) repository).getRelatedFileResources(entity);
  }

  @Override
  public List<FileResource> getRelatedFileResources(UUID entityUuid) {
    return ((EntityRepository<E>) repository).getRelatedFileResources(entityUuid);
  }

  @Override
  public E save(E entity) throws IdentifiableServiceException, ValidationException {
    try {
      E entityDb = super.save(entity);
      sendNotification("save", "POST", entityDb.getUuid(), entityDb.getEntityType());
      return entityDb;
    } catch (IdentifiableServiceException e) {
      LOGGER.error("Cannot save entity " + entity + ": ", e);
      throw e;
    }
  }

  @Override
  public List<FileResource> setRelatedFileResources(E entity, List<FileResource> fileResources) {
    return ((EntityRepository<E>) repository).setRelatedFileResources(entity, fileResources);
  }

  @Override
  public List<FileResource> setRelatedFileResources(
      UUID entityUuid, List<FileResource> fileResources) {
    return ((EntityRepository<E>) repository).setRelatedFileResources(entityUuid, fileResources);
  }

  /** Send a notification to an external url when an entity has changed */
  protected void sendNotification(
      String action, String httpVerb, UUID uuid, EntityType entityType) {
    Optional<String> hook = hookProperties.getHookForActionAndType(action, entityType);
    if (hook.isEmpty()) {
      // if no suitable hook is found, do nothing
      return;
    }
    URI url = buildNotificationUrl(hook.get(), uuid, entityType);
    if (url == null) {
      LOGGER.warn("No url given, ignoring.");
      return;
    }
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(url)
            .method(httpVerb, HttpRequest.BodyPublishers.noBody())
            .build();
    try {
      HttpResponse response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
      int statusCode = response.statusCode();
      if (statusCode >= 400) {
        LOGGER.warn(
            "Sending a notification to {} with verb {} gave an error status code {}.",
            url,
            httpVerb,
            statusCode);
      }
    } catch (InterruptedException | IOException e) {
      LOGGER.warn(
          "Something went wrong when sending a notification to {}: {}", url, e.getMessage());
    }
  }

  @Override
  public E update(E entity) throws IdentifiableServiceException, ValidationException {
    try {
      E entityDb = super.update(entity);
      sendNotification("update", "PUT", entityDb.getUuid(), entityDb.getEntityType());
      return entityDb;
    } catch (IdentifiableServiceException e) {
      LOGGER.error("Cannot update identifiable " + entity + ": ", e);
      throw e;
    }
  }
}
