package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.identifiable.CudamiIdentifiablesClient;
import de.digitalcollections.cudami.client.identifiable.CudamiIdentifierTypesClient;
import de.digitalcollections.cudami.client.identifiable.agent.CudamiFamilyNamesClient;
import de.digitalcollections.cudami.client.identifiable.agent.CudamiGivenNamesClient;
import de.digitalcollections.cudami.client.identifiable.alias.CudamiUrlAliasClient;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiArticlesClient;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiCollectionsClient;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiDigitalObjectsClient;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiEntitiesClient;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiProjectsClient;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiTopicsClient;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiWebsitesClient;
import de.digitalcollections.cudami.client.identifiable.entity.agent.CudamiCorporateBodiesClient;
import de.digitalcollections.cudami.client.identifiable.entity.agent.CudamiPersonsClient;
import de.digitalcollections.cudami.client.identifiable.entity.geo.location.CudamiGeoLocationsClient;
import de.digitalcollections.cudami.client.identifiable.entity.geo.location.CudamiHumanSettlementsClient;
import de.digitalcollections.cudami.client.identifiable.entity.relation.CudamiEntityRelationsClient;
import de.digitalcollections.cudami.client.identifiable.entity.work.CudamiItemsClient;
import de.digitalcollections.cudami.client.identifiable.entity.work.CudamiWorksClient;
import de.digitalcollections.cudami.client.identifiable.resource.CudamiFileResourcesBinaryClient;
import de.digitalcollections.cudami.client.identifiable.resource.CudamiFileResourcesMetadataClient;
import de.digitalcollections.cudami.client.identifiable.web.CudamiWebpagesClient;
import de.digitalcollections.cudami.client.relation.CudamiPredicatesClient;
import de.digitalcollections.cudami.client.security.CudamiUsersClient;
import de.digitalcollections.cudami.client.semantic.CudamiHeadwordsClient;
import de.digitalcollections.cudami.client.view.CudamiRenderingTemplatesClient;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.time.Duration;

public class CudamiClient {

  private final CudamiArticlesClient cudamiArticlesClient;
  private final CudamiCollectionsClient cudamiCollectionsClient;
  private final CudamiCorporateBodiesClient cudamiCorporateBodiesClient;
  private final CudamiDigitalObjectsClient cudamiDigitalObjectsClient;
  private final CudamiEntitiesClient cudamiEntitiesClient;
  private final CudamiEntityRelationsClient cudamiEntityRelationsClient;
  private final CudamiFamilyNamesClient cudamiFamilyNamesClient;
  private final CudamiFileResourcesBinaryClient cudamiFileResourcesBinaryClient;
  private final CudamiFileResourcesMetadataClient cudamiFileResourcesMetadataClient;
  private final CudamiGeoLocationsClient cudamiGeoLocationsClient;
  private final CudamiGivenNamesClient cudamiGivenNamesClient;
  private final CudamiHeadwordsClient cudamiHeadwordsClient;
  private final CudamiHumanSettlementsClient cudamiHumanSettlementsClient;
  private final CudamiIdentifiablesClient cudamiIdentifiablesClient;
  private final CudamiIdentifierTypesClient cudamiIdentifierTypesClient;
  private final CudamiItemsClient cudamiItemsClient;
  private final CudamiLocalesClient cudamiLocalesClient;
  private final CudamiPersonsClient cudamiPersonsClient;
  private final CudamiPredicatesClient cudamiPredicatesClient;
  private final CudamiProjectsClient cudamiProjectsClient;
  private final CudamiRenderingTemplatesClient cudamiRenderingTemplatesClient;
  private final CudamiTopicsClient cudamiTopicsClient;
  private final CudamiUrlAliasClient cudamiUrlAliasClient;
  private final CudamiUsersClient cudamiUsersClient;
  private final CudamiWebpagesClient cudamiWebpagesClient;
  private final CudamiWebsitesClient cudamiWebsitesClient;
  private final CudamiWorksClient cudamiWorksClient;
  protected final HttpClient http;

  public CudamiClient(String cudamiServerUrl, ObjectMapper mapper) {
    this(
        HttpClient.newBuilder()
            .followRedirects(Redirect.ALWAYS)
            .connectTimeout(Duration.ofSeconds(10))
            .version(Version.HTTP_1_1)
            .build(),
        cudamiServerUrl,
        mapper);
  }

  public CudamiClient(HttpClient http, String cudamiServerUrl, ObjectMapper mapper) {
    this.http = http;
    this.cudamiArticlesClient = new CudamiArticlesClient(http, cudamiServerUrl, mapper);
    this.cudamiCollectionsClient = new CudamiCollectionsClient(http, cudamiServerUrl, mapper);
    this.cudamiCorporateBodiesClient =
        new CudamiCorporateBodiesClient(http, cudamiServerUrl, mapper);
    this.cudamiDigitalObjectsClient = new CudamiDigitalObjectsClient(http, cudamiServerUrl, mapper);
    this.cudamiEntitiesClient = new CudamiEntitiesClient(http, cudamiServerUrl, mapper);
    this.cudamiEntityRelationsClient =
        new CudamiEntityRelationsClient(http, cudamiServerUrl, mapper);
    this.cudamiFamilyNamesClient = new CudamiFamilyNamesClient(http, cudamiServerUrl, mapper);
    this.cudamiFileResourcesBinaryClient =
        new CudamiFileResourcesBinaryClient(cudamiServerUrl, mapper);
    this.cudamiFileResourcesMetadataClient =
        new CudamiFileResourcesMetadataClient(http, cudamiServerUrl, mapper);
    this.cudamiGeoLocationsClient = new CudamiGeoLocationsClient(http, cudamiServerUrl, mapper);
    this.cudamiGivenNamesClient = new CudamiGivenNamesClient(http, cudamiServerUrl, mapper);
    this.cudamiHeadwordsClient = new CudamiHeadwordsClient(http, cudamiServerUrl, mapper);
    this.cudamiHumanSettlementsClient =
        new CudamiHumanSettlementsClient(http, cudamiServerUrl, mapper);
    this.cudamiIdentifiablesClient = new CudamiIdentifiablesClient(http, cudamiServerUrl, mapper);
    this.cudamiIdentifierTypesClient =
        new CudamiIdentifierTypesClient(http, cudamiServerUrl, mapper);
    this.cudamiItemsClient = new CudamiItemsClient(http, cudamiServerUrl, mapper);
    this.cudamiLocalesClient = new CudamiLocalesClient(http, cudamiServerUrl, mapper);
    this.cudamiPersonsClient = new CudamiPersonsClient(http, cudamiServerUrl, mapper);
    this.cudamiPredicatesClient = new CudamiPredicatesClient(http, cudamiServerUrl, mapper);
    this.cudamiProjectsClient = new CudamiProjectsClient(http, cudamiServerUrl, mapper);
    this.cudamiRenderingTemplatesClient =
        new CudamiRenderingTemplatesClient(http, cudamiServerUrl, mapper);
    this.cudamiTopicsClient = new CudamiTopicsClient(http, cudamiServerUrl, mapper);
    this.cudamiUrlAliasClient = new CudamiUrlAliasClient(http, cudamiServerUrl, mapper);
    this.cudamiUsersClient = new CudamiUsersClient(http, cudamiServerUrl, mapper);
    this.cudamiWebpagesClient = new CudamiWebpagesClient(http, cudamiServerUrl, mapper);
    this.cudamiWebsitesClient = new CudamiWebsitesClient(http, cudamiServerUrl, mapper);
    this.cudamiWorksClient = new CudamiWorksClient(http, cudamiServerUrl, mapper);
  }

  public CudamiArticlesClient forArticles() {
    return cudamiArticlesClient;
  }

  public CudamiCollectionsClient forCollections() {
    return cudamiCollectionsClient;
  }

  public CudamiCorporateBodiesClient forCorporateBodies() {
    return cudamiCorporateBodiesClient;
  }

  public CudamiDigitalObjectsClient forDigitalObjects() {
    return cudamiDigitalObjectsClient;
  }

  public CudamiEntitiesClient forEntities() {
    return cudamiEntitiesClient;
  }

  public CudamiEntityRelationsClient forEntityRelations() {
    return cudamiEntityRelationsClient;
  }

  public CudamiFamilyNamesClient forFamilyNames() {
    return cudamiFamilyNamesClient;
  }

  public CudamiFileResourcesBinaryClient forFileResourcesBinary() {
    return cudamiFileResourcesBinaryClient;
  }

  public CudamiFileResourcesMetadataClient forFileResourcesMetadata() {
    return cudamiFileResourcesMetadataClient;
  }

  public CudamiGeoLocationsClient forGeoLocations() {
    return cudamiGeoLocationsClient;
  }

  public CudamiGivenNamesClient forGivenNames() {
    return cudamiGivenNamesClient;
  }

  public CudamiHeadwordsClient forHeadwords() {
    return cudamiHeadwordsClient;
  }

  public CudamiHumanSettlementsClient forHumanSettlements() {
    return cudamiHumanSettlementsClient;
  }

  public CudamiIdentifiablesClient forIdentifiables() {
    return cudamiIdentifiablesClient;
  }

  public CudamiIdentifierTypesClient forIdentifierTypes() {
    return cudamiIdentifierTypesClient;
  }

  public CudamiItemsClient forItems() {
    return cudamiItemsClient;
  }

  public CudamiLocalesClient forLocales() {
    return cudamiLocalesClient;
  }

  public CudamiPersonsClient forPersons() {
    return cudamiPersonsClient;
  }

  public CudamiPredicatesClient forPredicates() {
    return cudamiPredicatesClient;
  }

  public CudamiProjectsClient forProjects() {
    return cudamiProjectsClient;
  }

  public CudamiRenderingTemplatesClient forRenderingTemplates() {
    return cudamiRenderingTemplatesClient;
  }

  public CudamiTopicsClient forTopics() {
    return cudamiTopicsClient;
  }

  public CudamiUrlAliasClient forUrlAliases() {
    return cudamiUrlAliasClient;
  }

  public CudamiUsersClient forUsers() {
    return cudamiUsersClient;
  }

  public CudamiWebpagesClient forWebpages() {
    return cudamiWebpagesClient;
  }

  public CudamiWebsitesClient forWebsites() {
    return cudamiWebsitesClient;
  }

  public CudamiWorksClient forWorks() {
    return cudamiWorksClient;
  }
}
