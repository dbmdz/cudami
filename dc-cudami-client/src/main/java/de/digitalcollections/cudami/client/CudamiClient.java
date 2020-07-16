package de.digitalcollections.cudami.client;

public class CudamiClient {

  private final CudamiArticlesClient cudamiArticlesClient;
  private final CudamiCollectionsClient cudamiCollectionsClient;
  private final CudamiCorporationsClient cudamiCorporationsClient;
  private final CudamiDigitalObjectsClient cudamiDigitalObjectsClient;
  private final CudamiEntitiesClient cudamiEntitiesClient;
  private final CudamiIdentifiablesClient cudamiIdentifiablesClient;
  private final CudamiProjectsClient cudamiProjectsClient;
  private final CudamiSubtopicsClient cudamiSubtopicsClient;
  private final CudamiSystemClient cudamiSystemClient;
  private final CudamiUsersClient cudamiUsersClient;
  private final CudamiWebpagesClient cudamiWebpagesClient;
  private final CudamiWebsitesClient cudamiWebsitesClient;

  public CudamiClient(String cudamiServerUrl) {
    this.cudamiArticlesClient = new CudamiArticlesClient(cudamiServerUrl);
    this.cudamiCollectionsClient = CudamiCollectionsClient.build(cudamiServerUrl);
    this.cudamiCorporationsClient = CudamiCorporationsClient.build(cudamiServerUrl);
    this.cudamiDigitalObjectsClient = CudamiDigitalObjectsClient.build(cudamiServerUrl);
    this.cudamiEntitiesClient = CudamiEntitiesClient.build(cudamiServerUrl);
    this.cudamiIdentifiablesClient = CudamiIdentifiablesClient.build(cudamiServerUrl);
    this.cudamiProjectsClient = CudamiProjectsClient.build(cudamiServerUrl);
    this.cudamiSubtopicsClient = new CudamiSubtopicsClient(cudamiServerUrl);
    this.cudamiSystemClient = CudamiSystemClient.build(cudamiServerUrl);
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

  public CudamiIdentifiablesClient forIdentifiables() {
    return cudamiIdentifiablesClient;
  }

  public CudamiProjectsClient forProjects() {
    return cudamiProjectsClient;
  }

  public CudamiSubtopicsClient forSubtopics() {
    return cudamiSubtopicsClient;
  }

  public CudamiSystemClient forSystem() {
    return cudamiSystemClient;
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
