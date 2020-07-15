package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.exceptions.CudamiRestErrorDecoder;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.entity.Project;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import feign.Headers;
import feign.Logger;
import feign.Param;
import feign.ReflectiveFeign;
import feign.RequestLine;
import feign.Retryer;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;
import java.util.UUID;

public interface CudamiProjectsClient {

  public static CudamiProjectsClient build(String serverUrl) {
    ObjectMapper mapper = new DigitalCollectionsObjectMapper();
    CudamiProjectsClient backend =
        ReflectiveFeign.builder()
            .decoder(new JacksonDecoder(mapper))
            .encoder(new JacksonEncoder(mapper))
            .errorDecoder(new CudamiRestErrorDecoder())
            .logger(new Slf4jLogger())
            .logLevel(Logger.Level.BASIC)
            .retryer(new Retryer.Default())
            .target(CudamiProjectsClient.class, serverUrl);
    return backend;
  }

  //  default Project createProject() {
  //    return new ProjectImpl();
  //  }
  //
  //  default PageResponse findProjects(PageRequest pageRequest) {
  //    FindParams f = new FindParamsImpl(pageRequest);
  //    PageResponse<Project> pageResponse =
  //        findProjects(
  //            f.getPageNumber(),
  //            f.getPageSize(),
  //            f.getSortField(),
  //            f.getSortDirection(),
  //            f.getNullHandling());
  //    return pageResponse;
  //  }

  @RequestLine(
      "GET /latest/projects?pageNumber={pageNumber}&pageSize={pageSize}&sortField={sortField}&sortDirection={sortDirection}&nullHandling={nullHandling}")
  PageResponse<Project> findProjects(
      @Param("pageNumber") int pageNumber,
      @Param("pageSize") int pageSize,
      @Param("sortField") String sortField,
      @Param("sortDirection") String sortDirection,
      @Param("nullHandling") String nullHandling);

  @RequestLine("GET /latest/projects/{uuid}")
  Project getProject(@Param("uuid") UUID uuid) throws HttpException;

  @RequestLine("POST /latest/projects/{parentProjectUuid}/project")
  @Headers("Content-Type: application/json")
  Project saveProjectWithParentProject(
      Project project, @Param("parentProjectUuid") UUID parentProjectUuid);

  @RequestLine("POST /latest/projects")
  @Headers("Content-Type: application/json")
  Project saveProject(Project project);

  //  default Project updateProject(Project project) {
  //    return updateProject(project.getUuid(), project);
  //  }

  @RequestLine("PUT /latest/projects/{uuid}")
  @Headers("Content-Type: application/json")
  Project updateProject(@Param("uuid") UUID uuid, Project project);
}
