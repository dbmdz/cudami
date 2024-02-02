package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.EntityService;
import de.digitalcollections.cudami.server.business.impl.service.identifiable.IdentifiableServiceImpl;
import de.digitalcollections.cudami.server.config.HookProperties;
import de.digitalcollections.model.identifiable.IdentifiableObjectType;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.validation.ValidationException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

// @Transactional should not be set in derived class to prevent overriding, check base class instead
@Service("entityService")
public class EntityServiceImpl<E extends Entity>
    extends IdentifiableServiceImpl<E, EntityRepository<E>> implements EntityService<E> {

  private static final Logger LOGGER = LoggerFactory.getLogger(EntityServiceImpl.class);

  protected HookProperties hookProperties;

  protected HttpClient httpClient = HttpClient.newHttpClient();

  public EntityServiceImpl(
      @Qualifier("entityRepositoryImpl") EntityRepository<E> repository,
      IdentifierService identifierService,
      UrlAliasService urlAliasService,
      HookProperties hookProperties,
      LocaleService localeService,
      CudamiConfig cudamiConfig) {
    super(repository, identifierService, urlAliasService, localeService, cudamiConfig);
    this.hookProperties = hookProperties;
  }

  @Override
  public void addRelatedFileresource(E entity, FileResource fileResource) throws ServiceException {
    try {
      repository.addRelatedFileresource(entity, fileResource);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  /**
   * Build a notification url by replacing placeholders in the template with the entity's uuid and
   * type
   */
  // TODO: externalize to Hook-/NotificationService and use Entity instead uuid as param
  protected URI buildNotificationUrl(
      String urlTemplate, UUID entityUuid, IdentifiableObjectType identifiableObjectType) {
    String url = String.format(urlTemplate, entityUuid, identifiableObjectType);
    try {
      return new URL(url).toURI();
    } catch (MalformedURLException | URISyntaxException e) {
      LOGGER.warn("Something went wrong when creating the notification url: {}", e.getMessage());
      return null;
    }
  }

  @Override
  public E getByRefId(long refId) throws ServiceException {
    try {
      return repository.getByRefId(refId);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public void save(E entity) throws ServiceException, ValidationException {
    try {
      super.save(entity);
      sendNotification("save", "POST", entity.getUuid(), entity.getIdentifiableObjectType());
    } catch (ServiceException e) {
      throw new ServiceException("Cannot save entity %s: %s".formatted(entity, e.getMessage()), e);
    }
  }

  /** Send a notification to an external url when an entity has changed */
  protected void sendNotification(
      String action, String httpVerb, UUID uuid, IdentifiableObjectType identifiableObjectType) {
    Optional<String> hook = hookProperties.getHookForActionAndType(action, identifiableObjectType);
    if (hook.isEmpty()) {
      // if no suitable hook is found, do nothing
      return;
    }
    URI url = buildNotificationUrl(hook.get(), uuid, identifiableObjectType);
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
  public void update(E entity) throws ServiceException, ValidationException {
    try {
      super.update(entity);
      sendNotification("update", "PUT", entity.getUuid(), entity.getIdentifiableObjectType());
    } catch (ServiceException e) {
      throw new ServiceException(
          "Cannot update identifiable %s: %s".formatted(entity, e.getMessage()), e);
    }
  }
}
