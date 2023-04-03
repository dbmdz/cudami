package de.digitalcollections.cudami.server.business.impl.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.IdentifiableObjectType;
import de.digitalcollections.model.identifiable.IdentifiableType;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.entity.item.Item;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractServiceImplTest {

  protected CudamiConfig cudamiConfig;

  private DigitalCollectionsObjectMapper mapper;

  @BeforeEach
  protected void beforeEach() throws Exception {
    mapper = new DigitalCollectionsObjectMapper();

    cudamiConfig = mock(CudamiConfig.class);

    CudamiConfig.UrlAlias urlAliasConfig = mock(CudamiConfig.UrlAlias.class);
    when(cudamiConfig.getUrlAlias()).thenReturn(urlAliasConfig);

    when(urlAliasConfig.getGenerationExcludes())
        .thenReturn(List.of("DIGITALOBJECT", "ITEM", "MANIFESTATION", "WORK"));
  }

  protected <O> O createDeepCopy(O object) {
    try {
      String serializedObject = mapper.writeValueAsString(object);
      O copy = (O) mapper.readValue(serializedObject, object.getClass());

      assertThat(copy).isEqualTo(object);
      return copy;
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Cannot serialize/deserialize " + object + ": " + e, e);
    }
  }

  protected Collection createCollection() {
    Collection collection = new Collection();
    collection.setUuid(UUID.randomUUID());
    return collection;
  }

  protected DigitalObject createDigitalObject() {
    DigitalObject digitalObject = new DigitalObject();
    digitalObject.setUuid(UUID.randomUUID());
    return digitalObject;
  }

  protected Entity createEntity() {
    Entity entity = new Entity();
    entity.setUuid(UUID.randomUUID());
    return entity;
  }

  protected FileResource createFileResource() {
    FileResource fileResource = new FileResource();
    fileResource.setUuid(UUID.randomUUID());
    return fileResource;
  }

  protected Identifiable createIdentifiable() {
    Identifiable identifiable = new Identifiable();
    identifiable.setUuid(UUID.randomUUID());
    return identifiable;
  }

  protected Item createItem() {
    Item item = new Item();
    item.setUuid(UUID.randomUUID());
    return item;
  }

  protected UrlAlias createUrlAlias() {
    UrlAlias urlAlias = new UrlAlias();
    urlAlias.setUuid(UUID.randomUUID());
    return urlAlias;
  }

  protected Webpage createWebpage() {
    Webpage webpage = new Webpage();
    webpage.setUuid(UUID.randomUUID());
    return webpage;
  }

  protected Website createWebsite() {
    Website website = new Website();
    website.setUuid(UUID.randomUUID());
    return website;
  }

  protected UrlAlias createUrlAlias(
      String slug,
      boolean setUuid,
      String language,
      boolean primary,
      UUID targetUuid,
      UUID websiteUuid) {
    Identifiable target = createIdentifiable();
    target.setIdentifiableObjectType(IdentifiableObjectType.COLLECTION);
    target.setType(IdentifiableType.ENTITY);

    UrlAlias urlAlias = new UrlAlias();
    if (setUuid) {
      urlAlias.setUuid(UUID.randomUUID());
    }
    urlAlias.setPrimary(primary);
    urlAlias.setTarget(target);
    urlAlias.setSlug(slug);
    urlAlias.setLastPublished(LocalDateTime.now());
    urlAlias.setCreated(LocalDateTime.now());
    urlAlias.setTargetLanguage(Locale.forLanguageTag(language));
    urlAlias.setWebsite(createWebsite(websiteUuid));
    return urlAlias;
  }

  protected Website createWebsite(UUID uuid) {
    Website website = new Website();
    website.setUuid(uuid);
    String dummyUrl = "http://" + uuid + "/";
    try {
      website.setUrl(new URL(dummyUrl));
    } catch (MalformedURLException e) {
      throw new RuntimeException("Cannot create dummy URL=" + dummyUrl + ": " + e, e);
    }
    return website;
  }
}
