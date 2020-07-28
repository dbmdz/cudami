package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.HttpClient;
import java.time.Duration;

public class CudamiClient {

  protected final HttpClient http;
  private final CudamiArticlesClient cudamiArticlesClient;
  private final CudamiCollectionsClient cudamiCollectionsClient;
  private final CudamiCorporationsClient cudamiCorporationsClient;
  private final CudamiDigitalObjectsClient cudamiDigitalObjectsClient;
  private final CudamiEntitiesClient cudamiEntitiesClient;
  private final CudamiEntityPartsClient cudamiEntityPartsClient;
  private final CudamiFileResourcesBinaryClient cudamiFileResourcesBinaryClient;
  private final CudamiFileResourcesMetadataClient cudamiFileResourcesMetadataClient;
  private final CudamiIdentifiablesClient cudamiIdentifiablesClient;
  private final CudamiIdentifierTypesClient cudamiIdentifierTypesClient;
  private final CudamiLocalesClient cudamiLocalesClient;
  private final CudamiProjectsClient cudamiProjectsClient;
  private final CudamiSubtopicsClient cudamiSubtopicsClient;
  private final CudamiTopicsClient cudamiTopicsClient;
  private final CudamiUsersClient cudamiUsersClient;
  private final CudamiWebpagesClient cudamiWebpagesClient;
  private final CudamiWebsitesClient cudamiWebsitesClient;

  public CudamiClient(String cudamiServerUrl, ObjectMapper mapper) {
    this(
        HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .connectTimeout(Duration.ofSeconds(10))
            .build(),
        cudamiServerUrl,
        mapper);
  }

  public CudamiClient(HttpClient http, String cudamiServerUrl, ObjectMapper mapper) {
    this.http = http;
    this.cudamiArticlesClient = new CudamiArticlesClient(http, cudamiServerUrl, mapper);
    this.cudamiCollectionsClient = new CudamiCollectionsClient(http, cudamiServerUrl, mapper);
    this.cudamiCorporationsClient = new CudamiCorporationsClient(http, cudamiServerUrl, mapper);
    this.cudamiDigitalObjectsClient = new CudamiDigitalObjectsClient(http, cudamiServerUrl, mapper);
    this.cudamiEntitiesClient = new CudamiEntitiesClient(http, cudamiServerUrl, mapper);
    this.cudamiEntityPartsClient = new CudamiEntityPartsClient(http, cudamiServerUrl, mapper);
    this.cudamiFileResourcesBinaryClient =
        new CudamiFileResourcesBinaryClient(cudamiServerUrl, mapper);
    this.cudamiFileResourcesMetadataClient =
        new CudamiFileResourcesMetadataClient(http, cudamiServerUrl, mapper);
    this.cudamiIdentifiablesClient = new CudamiIdentifiablesClient(http, cudamiServerUrl, mapper);
    this.cudamiIdentifierTypesClient =
        new CudamiIdentifierTypesClient(http, cudamiServerUrl, mapper);
    this.cudamiLocalesClient = new CudamiLocalesClient(http, cudamiServerUrl, mapper);
    this.cudamiProjectsClient = new CudamiProjectsClient(http, cudamiServerUrl, mapper);
    this.cudamiSubtopicsClient = new CudamiSubtopicsClient(http, cudamiServerUrl, mapper);
    this.cudamiTopicsClient = new CudamiTopicsClient(http, cudamiServerUrl, mapper);
    this.cudamiUsersClient = new CudamiUsersClient(http, cudamiServerUrl, mapper);
    this.cudamiWebpagesClient = new CudamiWebpagesClient(http, cudamiServerUrl, mapper);
    this.cudamiWebsitesClient = new CudamiWebsitesClient(http, cudamiServerUrl, mapper);
  }

  public CudamiArticlesClient forArticles() {
    return cudamiArticlesClient;
  }

  public CudamiCollectionsClient forCollections() {
    return cudamiCollectionsClient;
  }

  public CudamiCorporationsClient forCorporations() {
    return cudamiCorporationsClient;
  }

  public CudamiDigitalObjectsClient forDigitalObjects() {
    return cudamiDigitalObjectsClient;
  }

  public CudamiEntitiesClient forEntities() {
    return cudamiEntitiesClient;
  }

  public CudamiEntityPartsClient forEntityParts() {
    return cudamiEntityPartsClient;
  }

  public CudamiFileResourcesBinaryClient forFileResourcesBinary() {
    return cudamiFileResourcesBinaryClient;
  }

  public CudamiFileResourcesMetadataClient forFileResourcesMetadata() {
    return cudamiFileResourcesMetadataClient;
  }

  public CudamiIdentifiablesClient forIdentifiables() {
    return cudamiIdentifiablesClient;
  }

  public CudamiIdentifierTypesClient forIdentifierTypes() {
    return cudamiIdentifierTypesClient;
  }

  public CudamiLocalesClient forLocales() {
    return cudamiLocalesClient;
  }

  public CudamiProjectsClient forProjects() {
    return cudamiProjectsClient;
  }

  public CudamiSubtopicsClient forSubtopics() {
    return cudamiSubtopicsClient;
  }

  public CudamiTopicsClient forTopics() {
    return cudamiTopicsClient;
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
}
