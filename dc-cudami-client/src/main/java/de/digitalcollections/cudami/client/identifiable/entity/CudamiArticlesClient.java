package de.digitalcollections.cudami.client.identifiable.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.exception.http.client.ResourceNotFoundException;
import de.digitalcollections.model.identifiable.entity.Article;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CudamiArticlesClient extends CudamiEntitiesClient<Article> {

  public CudamiArticlesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Article.class, mapper, API_VERSION_PREFIX + "/articles");
  }

  public Article getByUuid(UUID uuid, Locale locale) throws TechnicalException {
    try {
      if (locale != null) {
        return doGetRequestForObject(String.format(baseEndpoint + "/%s?pLocale=%s", uuid, locale));
      } else {
        return doGetRequestForObject(String.format(baseEndpoint + "/%s", uuid));
      }
    } catch (ResourceNotFoundException e) {
      return null;
    }
  }

  public boolean removeCreator(UUID articleUuid, UUID agentUuid) throws TechnicalException {
    try {
      doDeleteRequestForString(
          String.format(baseEndpoint + "/%s/creators/%s", articleUuid, agentUuid));
    } catch (ResourceNotFoundException e) {
      return false;
    }
    return true;
  }

  public boolean addCreators(UUID articleUuid, List<Agent> agents) throws TechnicalException {
    try {
      doPostRequestForString(String.format(baseEndpoint + "/%s/creators", articleUuid), agents);
    } catch (ResourceNotFoundException e) {
      return false;
    }
    return true;
  }
}
