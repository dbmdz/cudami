package de.digitalcollections.cudami.admin.actuator;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import de.digitalcollections.cudami.admin.model.monitoring.VersionInfo;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

@Component
@Endpoint(id = "version")
public class VersionActuatorEndpoint {

  private static VersionInfo versionInfo = null;

  public VersionActuatorEndpoint(VersionInfo versionInfo) {
    this.versionInfo = versionInfo;
  }

  @ReadOperation
  public VersionResponse getVersion() {
    return new VersionResponse(
        versionInfo.getApplicationName(),
        versionInfo.getVersionInfo(),
        versionInfo.getBuildDetails());
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonPropertyOrder({"name", "version", "details"})
  public static class VersionResponse {

    @JsonProperty private String name;

    @JsonProperty private String version;

    @JsonProperty private String details;

    public VersionResponse(String name, String version, String details) {
      this.name = name;
      this.version = version;
      this.details = details;
    }
  }
}
