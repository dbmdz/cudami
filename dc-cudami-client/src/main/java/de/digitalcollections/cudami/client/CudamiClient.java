package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CudamiClient {

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
    this.cudamiArticlesClient = new CudamiArticlesClient(cudamiServerUrl, mapper);
    this.cudamiCollectionsClient = new CudamiCollectionsClient(cudamiServerUrl, mapper);
    this.cudamiCorporationsClient = new CudamiCorporationsClient(cudamiServerUrl, mapper);
    this.cudamiDigitalObjectsClient = new CudamiDigitalObjectsClient(cudamiServerUrl, mapper);
    this.cudamiEntitiesClient = new CudamiEntitiesClient(cudamiServerUrl, mapper);
    this.cudamiEntityPartsClient = new CudamiEntityPartsClient(cudamiServerUrl, mapper);
    this.cudamiFileResourcesBinaryClient =
        new CudamiFileResourcesBinaryClient(cudamiServerUrl, mapper);
    this.cudamiFileResourcesMetadataClient =
        new CudamiFileResourcesMetadataClient(cudamiServerUrl, mapper);
    this.cudamiIdentifiablesClient = new CudamiIdentifiablesClient(cudamiServerUrl, mapper);
    this.cudamiIdentifierTypesClient = new CudamiIdentifierTypesClient(cudamiServerUrl, mapper);
    this.cudamiLocalesClient = new CudamiLocalesClient(cudamiServerUrl, mapper);
    this.cudamiProjectsClient = new CudamiProjectsClient(cudamiServerUrl, mapper);
    this.cudamiSubtopicsClient = new CudamiSubtopicsClient(cudamiServerUrl, mapper);
    this.cudamiTopicsClient = new CudamiTopicsClient(cudamiServerUrl, mapper);
    this.cudamiUsersClient = new CudamiUsersClient(cudamiServerUrl, mapper);
    this.cudamiWebpagesClient = new CudamiWebpagesClient(cudamiServerUrl, mapper);
    this.cudamiWebsitesClient = new CudamiWebsitesClient(cudamiServerUrl, mapper);
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
