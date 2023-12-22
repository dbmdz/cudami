package de.digitalcollections.cudami.server.actuator;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import de.digitalcollections.cudami.server.monitoring.VersionInfo;
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
  public static record VersionResponse(String name, String version, String details) {}
}
