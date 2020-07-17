package de.digitalcollections.cudami.client;

public class CudamiClient {

  private final CudamiArticlesClient cudamiArticlesClient;
  private final CudamiCollectionsClient cudamiCollectionsClient;
  private final CudamiCorporationsClient cudamiCorporationsClient;
  private final CudamiDigitalObjectsClient cudamiDigitalObjectsClient;
  private final CudamiEntitiesClient cudamiEntitiesClient;
  private final CudamiFileResourcesMetadataClient cudamiFileResourcesMetadataClient;
  private final CudamiIdentifiablesClient cudamiIdentifiablesClient;
  private final CudamiLocalesClient cudamiLocalesClient;
  private final CudamiProjectsClient cudamiProjectsClient;
  private final CudamiSubtopicsClient cudamiSubtopicsClient;
  private final CudamiTopicsClient cudamiTopicsClient;
  private final CudamiUsersClient cudamiUsersClient;
  private final CudamiWebpagesClient cudamiWebpagesClient;
  private final CudamiWebsitesClient cudamiWebsitesClient;

  public CudamiClient(String cudamiServerUrl) {
    this.cudamiArticlesClient = new CudamiArticlesClient(cudamiServerUrl);
    this.cudamiCollectionsClient = new CudamiCollectionsClient(cudamiServerUrl);
    this.cudamiCorporationsClient = new CudamiCorporationsClient(cudamiServerUrl);
    this.cudamiDigitalObjectsClient = new CudamiDigitalObjectsClient(cudamiServerUrl);
    this.cudamiEntitiesClient = CudamiEntitiesClient.build(cudamiServerUrl);
    this.cudamiFileResourcesMetadataClient = new CudamiFileResourcesMetadataClient(cudamiServerUrl);
    this.cudamiIdentifiablesClient = CudamiIdentifiablesClient.build(cudamiServerUrl);
    this.cudamiLocalesClient = new CudamiLocalesClient(cudamiServerUrl);
    this.cudamiProjectsClient = new CudamiProjectsClient(cudamiServerUrl);
    this.cudamiSubtopicsClient = new CudamiSubtopicsClient(cudamiServerUrl);
    this.cudamiTopicsClient = new CudamiTopicsClient(cudamiServerUrl);
    this.cudamiUsersClient = new CudamiUsersClient(cudamiServerUrl);
    this.cudamiWebpagesClient = new CudamiWebpagesClient(cudamiServerUrl);
    this.cudamiWebsitesClient = new CudamiWebsitesClient(cudamiServerUrl);
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

  public CudamiFileResourcesMetadataClient forFileResourcesMetadata() {
    return cudamiFileResourcesMetadataClient;
  }

  public CudamiIdentifiablesClient forIdentifiables() {
    return cudamiIdentifiablesClient;
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
