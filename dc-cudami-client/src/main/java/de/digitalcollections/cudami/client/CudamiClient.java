package de.digitalcollections.cudami.client;

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

  public CudamiClient(String cudamiServerUrl) {
    this.cudamiArticlesClient = new CudamiArticlesClient(cudamiServerUrl);
    this.cudamiCollectionsClient = new CudamiCollectionsClient(cudamiServerUrl);
    this.cudamiCorporationsClient = new CudamiCorporationsClient(cudamiServerUrl);
    this.cudamiDigitalObjectsClient = new CudamiDigitalObjectsClient(cudamiServerUrl);
    this.cudamiEntitiesClient = new CudamiEntitiesClient(cudamiServerUrl);
    this.cudamiEntityPartsClient = new CudamiEntityPartsClient(cudamiServerUrl);
    this.cudamiFileResourcesBinaryClient = new CudamiFileResourcesBinaryClient(cudamiServerUrl);
    this.cudamiFileResourcesMetadataClient = new CudamiFileResourcesMetadataClient(cudamiServerUrl);
    this.cudamiIdentifiablesClient = new CudamiIdentifiablesClient(cudamiServerUrl);
    this.cudamiIdentifierTypesClient = new CudamiIdentifierTypesClient(cudamiServerUrl);
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
