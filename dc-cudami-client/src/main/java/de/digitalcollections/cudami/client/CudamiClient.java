package de.digitalcollections.cudami.client;

public class CudamiClient {

  private final CudamiCollectionsClient cudamiCollectionsClient;
  private final CudamiCorporationsClient cudamiCorporationsClient;
  private final CudamiProjectsClient cudamiProjectsClient;
  private final CudamiSystemClient cudamiSystemClient;
  private final CudamiWebpagesClient cudamiWebpagesClient;

  public CudamiClient(String cudamiServerUrl) {
    this.cudamiCollectionsClient = CudamiCollectionsClient.build(cudamiServerUrl);
    this.cudamiCorporationsClient = CudamiCorporationsClient.build(cudamiServerUrl);
    this.cudamiProjectsClient = CudamiProjectsClient.build(cudamiServerUrl);
    this.cudamiSystemClient = CudamiSystemClient.build(cudamiServerUrl);
    this.cudamiWebpagesClient = CudamiWebpagesClient.build(cudamiServerUrl);
  }

  public CudamiCollectionsClient forCollections() {
    return cudamiCollectionsClient;
  }

  public CudamiCorporationsClient forCorporations() {
    return cudamiCorporationsClient;
  }

  public CudamiProjectsClient forProjects() {
    return cudamiProjectsClient;
  }

  public CudamiSystemClient forSystem() {
    return cudamiSystemClient;
  }

  public CudamiWebpagesClient forWebpages() {
    return cudamiWebpagesClient;
  }
}
